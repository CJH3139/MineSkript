package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClosingRoundTest {
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

    private void rename(Path root, String from, String to, String source) throws IOException {
        Files.delete(root.resolve(from));
        Files.writeString(root.resolve(to), source, StandardCharsets.UTF_8);
    }

    private void start(Path root) {
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), root.resolve("variables.json"), variables, game);
        dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler, variables, () -> {
        });
        service = new ScriptService(root, new ScriptLoader(new Parser(DefaultSyntax.registry())), registry, dispatcher, persistence, new ConfigFile(root.resolve(Config.NAME)), () -> nanos += 1_000_000L);
        service.start();
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    private List<String> files() {
        return registry.scripts().stream().map(ParsedScript::file).toList();
    }

    private static List<String> strings(List<ParseError> errors) {
        return errors.stream().map(Object::toString).toList();
    }

    @Test
    void aCaseOnlyRenameOnDiskReloadsTheScriptInsteadOfRegisteringASecondCopy(@TempDir Path root) throws IOException {
        write(root, "Mining.sk", "on chat:\n    send \"old\"\n");
        start(root);
        assertEquals(List.of("Mining.sk"), files());
        rename(root, "Mining.sk", "mining.sk", "on chat:\n    send \"new\"\n");
        assertEquals(List.of("mining.sk"), ScriptNames.list(root));
        FileReload result = service.reloadFile("mining.sk");
        assertEquals(FileReload.Outcome.RELOADED, result.outcome());
        assertEquals("mining.sk", result.file());
        assertEquals(List.of("mining.sk"), files());
        assertEquals(1, registry.triggers().size());
        assertEquals("mining.sk", registry.triggers().get(0).file());
        dispatcher.onChat("hi");
        assertEquals(List.of("new"), game.messages);
    }

    @Test
    void aCaseOnlyRenameDropsTheWaitingFramesOfTheVersionItReplaces(@TempDir Path root) throws IOException {
        write(root, "Mining.sk", "on chat:\n    wait 30 ticks\n    send \"old\"\n");
        start(root);
        dispatcher.onChat("hi");
        ticks(10);
        assertEquals(1, scheduler.size());
        rename(root, "Mining.sk", "mining.sk", "on chat:\n    send \"new\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("mining.sk").outcome());
        assertEquals(0, scheduler.size());
        ticks(60);
        assertEquals(List.of(), game.messages);
        dispatcher.onChat("hi");
        assertEquals(List.of("new"), game.messages);
    }

    @Test
    void aCaseOnlyRenameClearsTheErrorsFiledUnderTheOldSpelling(@TempDir Path root) throws IOException {
        write(root, "Mining.sk", "on chat:\n    fly\n");
        start(root);
        assertEquals(List.of("Mining.sk:2: unknown effect \"fly\""), strings(service.errors()));
        rename(root, "Mining.sk", "mining.sk", "on chat:\n    send \"new\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("mining.sk").outcome());
        assertEquals(List.of(), strings(service.errors()));
    }

    @Test
    void aWrongCaseReloadFilesItsErrorsUnderTheNameTheNextReloadClears(@TempDir Path root) throws IOException {
        write(root, "Mining.sk", "on chat:\n    send \"old\"\n");
        start(root);
        write(root, "Mining.sk", "on chat:\n    fly\n");
        FileReload kept = service.reloadFile("mining.sk");
        assertEquals(FileReload.Outcome.KEPT, kept.outcome());
        assertEquals("Mining.sk", kept.file());
        assertEquals(List.of("Mining.sk:2: unknown effect \"fly\""), strings(service.errors()));
        write(root, "Mining.sk", "on chat:\n    send \"new\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("mining.sk").outcome());
        assertEquals(List.of(), strings(service.errors()));
    }

    @Test
    void anExactNameWinsOverACaseDifferingOneWhenAFolderHoldsBoth(@TempDir Path root) throws IOException {
        List<String> both = List.of("Mining.sk", "mining.sk");
        assertEquals(Optional.of("mining.sk"), ScriptNames.pick(both, "mining.sk"));
        assertEquals(Optional.of("Mining.sk"), ScriptNames.pick(both, "Mining.sk"));
        assertEquals(Optional.of("Mining.sk"), ScriptNames.pick(List.of("Mining.sk"), "MINING.sk"));
        assertEquals(Optional.empty(), ScriptNames.pick(both, "chat.sk"));
        write(root, "Chat.sk", "on chat:\n    send \"a\"\n");
        assertEquals(Optional.of("Chat.sk"), ScriptNames.onDisk(root, "chat.sk"));
        assertEquals(Optional.of("Chat.sk"), ScriptNames.onDisk(root, "Chat.sk"));
    }

    @Test
    void aNameOnlyTheFilesystemCanResolveIsMissingRatherThanASecondCopy(@TempDir Path root) throws IOException {
        write(root, "my-mining-script.sk", "on chat:\n    send \"a\"\n");
        start(root);
        String alias = "my-min~1.sk";
        Assumptions.assumeTrue(Files.isRegularFile(root.resolve(alias)));
        assertEquals(List.of("my-mining-script.sk"), ScriptNames.list(root));
        FileReload result = service.reloadFile(alias);
        assertEquals(FileReload.Outcome.MISSING, result.outcome());
        assertEquals(List.of("my-mining-script.sk"), files());
        assertEquals(1, registry.triggers().size());
        dispatcher.onChat("hi");
        assertEquals(List.of("a"), game.messages);
    }

    @Test
    void aFolderWithNoScriptsIsNotTheSameAnswerAsAFolderThatCannotBeRead(@TempDir Path root) throws IOException {
        Path empty = Files.createDirectories(root.resolve("empty"));
        ScriptNames.Listing none = ScriptNames.listing(empty);
        assertTrue(none.readable());
        assertEquals(List.of(), none.names());
        write(root, "a.sk", "on chat:\n    send \"a\"\n");
        ScriptNames.Listing one = ScriptNames.listing(root);
        assertTrue(one.readable());
        assertEquals(List.of("a.sk"), one.names());
        ScriptNames.Listing gone = ScriptNames.listing(root.resolve("nope"));
        assertFalse(gone.readable());
        assertEquals(List.of(), gone.names());
        assertFalse(ScriptNames.listing(root.resolve("a.sk")).readable());
    }

    @Test
    void aFullLoadForgetsTheSourceLinesOfScriptsThatAreGone(@TempDir Path root) throws IOException {
        write(root, "a.sk", "on chat:\n    fly\n");
        start(root);
        assertEquals("fly", service.sources().line("a.sk", 2));
        Files.delete(root.resolve("a.sk"));
        service.reload();
        assertEquals(List.of(), files());
        assertEquals("", service.sources().line("a.sk", 2));
    }
}
