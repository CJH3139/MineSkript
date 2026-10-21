package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReloadFileTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final Variables variables = new Variables();
    private long nanos;
    private EventDispatcher dispatcher;
    private ScriptService service;

    private void write(Path root, String name, String source) throws IOException {
        Files.writeString(root.resolve(name), source, StandardCharsets.UTF_8);
    }

    private void start(Path root, SyntaxRegistry syntax) {
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), root.resolve("variables.json"), variables, game);
        dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler, variables, () -> {
        });
        service = new ScriptService(root, new ScriptLoader(new Parser(syntax)), registry, dispatcher, persistence, new ConfigFile(root.resolve(Config.NAME)), () -> nanos += 1_000_000L);
        service.start();
    }

    private void start(Path root) {
        start(root, DefaultSyntax.registry());
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    private List<String> files() {
        return registry.scripts().stream().map(ParsedScript::file).toList();
    }

    @Test
    void reloadingOneFileReplacesItsTriggersAndLeavesTheOtherFileRunning(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    send \"a1\"\n");
        write(root, "b.sk", "on chat:\n    send \"b\"\n");
        start(root);
        ticks(1);
        dispatcher.onChat("hi");
        assertEquals(List.of("a1", "b"), game.messages);
        game.messages.clear();
        write(root, "a.sk", "on chat:\n    send \"a2\"\n");
        FileReload result = service.reloadFile("a.sk");
        assertEquals(FileReload.Outcome.RELOADED, result.outcome());
        assertEquals(1, result.triggerCount());
        assertEquals(List.of(), result.errors());
        assertEquals(1L, result.millis());
        assertEquals(2L, service.lastMillis());
        dispatcher.onChat("hi");
        assertEquals(List.of("a2", "b"), game.messages);
    }

    @Test
    void aSingleFileReloadLeavesTheTickCounterAndTheVariablesAlone(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on load:\n    set {n} to 7\n");
        start(root);
        ticks(30);
        assertEquals(30L, dispatcher.ticks());
        write(root, "a.sk", "on load:\n    send \"n is %{n}%\"\n");
        service.reloadFile("a.sk");
        assertEquals(30L, dispatcher.ticks());
        assertEquals(List.of("n is 7"), game.messages);
    }

    @Test
    void aSingleFileReloadFiresOnScriptLoadForThatFileOnly(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on script load:\n    send \"a loaded\"\n");
        write(root, "b.sk", "on script load:\n    send \"b loaded\"\n");
        start(root);
        assertEquals(List.of("a loaded", "b loaded"), game.messages);
        game.messages.clear();
        service.reloadFile("b.sk");
        assertEquals(List.of("b loaded"), game.messages);
    }

    @Test
    void aSingleFileReloadThatFailsToParseKeepsTheVersionAlreadyRunning(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    send \"old\"\n");
        start(root);
        write(root, "a.sk", "on chat:\n    fly\n");
        FileReload result = service.reloadFile("a.sk");
        assertEquals(FileReload.Outcome.KEPT, result.outcome());
        assertEquals(List.of("a.sk:2: unknown effect \"fly\""), result.errors().stream().map(Object::toString).toList());
        assertEquals(1, result.triggerCount());
        dispatcher.onChat("hi");
        assertEquals(List.of("old"), game.messages);
        assertEquals(List.of("a.sk:2: unknown effect \"fly\""), service.errors().stream().map(Object::toString).toList());
    }

    @Test
    void aFileThatIsNewSinceTheLastFullLoadIsAddedEvenIfItDoesNotParse(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    send \"a\"\n");
        start(root);
        write(root, "b.sk", "on chat:\n    send \"b\"\n");
        FileReload added = service.reloadFile("b.sk");
        assertEquals(FileReload.Outcome.ADDED, added.outcome());
        assertEquals(1, added.triggerCount());
        dispatcher.onChat("hi");
        assertEquals(List.of("a", "b"), game.messages);
        write(root, "c.sk", "on chat:\n    fly\n");
        FileReload broken = service.reloadFile("c.sk");
        assertEquals(FileReload.Outcome.ADDED, broken.outcome());
        assertEquals(0, broken.triggerCount());
        assertEquals(List.of("c.sk:2: unknown effect \"fly\""), broken.errors().stream().map(Object::toString).toList());
        assertEquals(List.of("a.sk", "b.sk", "c.sk"), files());
    }

    @Test
    void reloadingAFileThatIsGoneRemovesItAndDropsItsFrames(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    send \"a\"\n");
        write(root, "b.sk", "every 100 ticks:\n    wait 500 ticks\n    send \"b\"\n");
        start(root);
        ticks(100);
        assertEquals(1, scheduler.size());
        Files.delete(root.resolve("b.sk"));
        FileReload result = service.reloadFile("b.sk");
        assertEquals(FileReload.Outcome.REMOVED, result.outcome());
        assertEquals(0, scheduler.size());
        assertEquals(List.of("a.sk"), files());
        ticks(600);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void reloadingANameThatWasNeverThereChangesNothing(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    send \"a\"\n");
        start(root);
        assertEquals(FileReload.Outcome.MISSING, service.reloadFile("nope.sk").outcome());
        assertEquals(FileReload.Outcome.REFUSED, service.reloadFile("notes.txt").outcome());
        assertEquals(FileReload.Outcome.MISSING, service.reloadFile("sub/a.sk").outcome());
        assertEquals(List.of("a.sk"), files());
    }

    @Test
    void reloadAllReloadsEveryScriptWhenNothingElseIsRunning(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    send \"a1\"\n");
        start(root);
        dispatcher.onChat("hi");
        assertEquals(List.of("a1"), game.messages);
        game.messages.clear();
        write(root, "a.sk", "on chat:\n    send \"a2\"\n");
        write(root, "b.sk", "on chat:\n    send \"b\"\n");
        LoadReport report = service.reloadAll();
        assertEquals(2, report.scriptCount());
        assertEquals(2, report.triggerCount());
        assertEquals(List.of("a.sk", "b.sk"), files());
        dispatcher.onChat("hi");
        assertEquals(List.of("a2", "b"), game.messages);
    }

    @Test
    void aReEntrantReloadAllLeavesTheGlobalVariablesInMemoryUntouched(@TempDir Path root) throws IOException {
        List<String> seen = new ArrayList<>();
        SyntaxRegistry syntax = new SyntaxRegistry();
        syntax.addEffect((match, scope) -> Optional.<Statement>of(new Statement() {
            @Override
            public int line() {
                return scope.line();
            }

            @Override
            public Flow execute(Context context) {
                seen.add("all:" + service.reloadAll());
                return Flow.CONTINUE;
            }
        }), "reenter reload all");
        DefaultSyntax.registerAll(syntax);
        write(root, "a.sk", "on script load:\n    set {n} to 7\n    reenter reload all\n    send \"n is %{n}%\"\n");
        start(root, syntax);
        assertEquals(List.of("all:null"), seen);
        assertEquals(List.of("n"), List.copyOf(variables.global().keySet()));
        assertEquals(List.of("n is 7"), game.messages);
    }

    @Test
    void everyReloadEntryPointSitsBehindTheSameReEntryGuard(@TempDir Path root) throws IOException {
        List<String> seen = new ArrayList<>();
        SyntaxRegistry syntax = new SyntaxRegistry();
        syntax.addEffect((match, scope) -> Optional.<Statement>of(new Statement() {
            @Override
            public int line() {
                return scope.line();
            }

            @Override
            public Flow execute(Context context) {
                seen.add("full:" + service.reload());
                seen.add("file:" + service.reloadFile("a.sk").outcome());
                seen.add("variables:" + service.reloadVariables());
                seen.add("all:" + service.reloadAll());
                return Flow.CONTINUE;
            }
        }), "reenter reload");
        DefaultSyntax.registerAll(syntax);
        write(root, "a.sk", "on script load:\n    reenter reload\n");
        start(root, syntax);
        assertEquals(List.of("full:null", "file:BUSY", "variables:BUSY", "all:null"), seen);
        assertEquals(List.of("a.sk"), files());
        assertEquals(1L, service.lastMillis());
    }

    @Test
    void aSuccessfulSingleFileReloadStopsOnlyThatFilesOutstandingWait(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on script load:\n    wait 50 ticks\n    send \"a old\"\n");
        write(root, "b.sk", "on script load:\n    wait 50 ticks\n    send \"b\"\n");
        start(root);
        assertEquals(2, scheduler.size());
        ticks(10);
        assertEquals(List.of(), game.messages);
        write(root, "a.sk", "on script load:\n    wait 20 ticks\n    send \"a new\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("a.sk").outcome());
        assertEquals(2, scheduler.size());
        ticks(20);
        assertEquals(List.of("a new"), game.messages);
        ticks(20);
        assertEquals(List.of("a new", "b"), game.messages);
        ticks(600);
        assertEquals(List.of("a new", "b"), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aSingleFileReloadRefusesABackslashNameThatEscapesTheScriptsFolder(@TempDir Path root) throws IOException {
        Path scripts = Files.createDirectories(root.resolve("scripts"));
        write(root, "secret.sk", "on chat:\n    send \"secret\"\n");
        write(scripts, "a.sk", "on chat:\n    send \"a\"\n");
        start(scripts);
        assertEquals(FileReload.Outcome.REFUSED, service.reloadFile("..\\secret.sk").outcome());
        assertEquals(FileReload.Outcome.REFUSED, service.reloadFile("sub\\..\\..\\secret.sk").outcome());
        assertEquals(List.of("a.sk"), files());
        dispatcher.onChat("hi");
        assertEquals(List.of("a"), game.messages);
    }

    @Test
    void reloadingAFileThatIsGoneAlsoClearsItsParseErrors(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    send \"a\"\n");
        write(root, "b.sk", "on chat:\n    fly\n");
        start(root);
        assertEquals(List.of("b.sk:2: unknown effect \"fly\""), service.errors().stream().map(Object::toString).toList());
        Files.delete(root.resolve("b.sk"));
        assertEquals(FileReload.Outcome.REMOVED, service.reloadFile("b.sk").outcome());
        assertEquals(List.of(), service.errors());
        assertEquals(List.of("a.sk"), files());
    }

    @Test
    void scriptsInFoldersLoadAndReloadByTheirPath(@TempDir Path root) throws IOException {
        Files.createDirectories(root.resolve("pvp"));
        Files.createDirectories(root.resolve("-disabled"));
        write(root, "a.sk", "on chat:\n    send \"a\"\n");
        write(root, "pvp/combat.sk", "on chat:\n    send \"combat 1\"\n");
        write(root, "-disabled/off.sk", "on chat:\n    send \"off\"\n");
        start(root);
        assertEquals(List.of("a.sk", "pvp/combat.sk"), files());
        dispatcher.onChat("hi");
        assertEquals(List.of("a", "combat 1"), game.messages);
        game.messages.clear();
        write(root, "pvp/combat.sk", "on chat:\n    send \"combat 2\"\n    fly\n");
        FileReload kept = service.reloadFile("pvp\\Combat.sk");
        assertEquals(FileReload.Outcome.KEPT, kept.outcome());
        assertEquals("pvp/combat.sk", kept.file());
        assertEquals("pvp/combat.sk", kept.errors().get(0).file());
        assertEquals("fly", service.sources().line("pvp/combat.sk", 3));
        write(root, "pvp/combat.sk", "on chat:\n    send \"combat 2\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("pvp/combat.sk").outcome());
        dispatcher.onChat("hi");
        assertEquals(List.of("a", "combat 2"), game.messages);
        assertEquals(FileReload.Outcome.REFUSED, service.reloadFile("-disabled/off.sk").outcome());
    }
}
