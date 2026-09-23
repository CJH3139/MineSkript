package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
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
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServiceCoverageTest {
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

    private void start(Path root) {
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), root.resolve("variables.json"), variables, game);
        dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler, variables, () -> {
        });
        service = new ScriptService(root, new ScriptLoader(new Parser(DefaultSyntax.registry())), registry, dispatcher, persistence, () -> nanos += 1_000_000L);
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

    private List<String> errorText() {
        return service.errors().stream().map(Object::toString).toList();
    }

    @Test
    void aSingleFileReloadRefusesAForwardSlashNameThatEscapesTheScriptsFolder(@TempDir Path root) throws IOException {
        Path scripts = Files.createDirectories(root.resolve("scripts"));
        write(root, "secret.ms", "on chat:\n    send \"secret\"\n");
        write(scripts, "a.ms", "on chat:\n    send \"a\"\n");
        start(scripts);
        assertEquals(FileReload.Outcome.REFUSED, service.reloadFile("../secret.ms").outcome());
        assertEquals(FileReload.Outcome.REFUSED, service.reloadFile("sub/a.ms").outcome());
        assertEquals(List.of("a.ms"), files());
        dispatcher.onChat("hi");
        assertEquals(List.of("a"), game.messages);
    }

    @Test
    void aFileThatIsFixedAndReloadedStopsReportingItsOldErrors(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    fly\n");
        start(root);
        assertEquals(List.of("a.ms:2: unknown effect \"fly\""), errorText());
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("a.ms").outcome());
        assertEquals(List.of(), service.errors());
    }

    @Test
    void aBrokenFileAddedByASingleFileReloadReachesTheErrorList(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        start(root);
        assertEquals(List.of(), service.errors());
        write(root, "b.ms", "on chat:\n    fly\n");
        assertEquals(FileReload.Outcome.ADDED, service.reloadFile("b.ms").outcome());
        assertEquals(List.of("b.ms:2: unknown effect \"fly\""), errorText());
    }

    @Test
    void aFullReloadStopsTheFramesOfTheVersionItReplaces(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on script load:\n    wait 50 ticks\n    send \"a old\"\n");
        write(root, "b.ms", "on script load:\n    wait 70 ticks\n    send \"b old\"\n");
        start(root);
        assertEquals(2, scheduler.size());
        ticks(10);
        assertEquals(List.of(), game.messages);
        write(root, "a.ms", "on script load:\n    wait 20 ticks\n    send \"a new\"\n");
        write(root, "b.ms", "on script load:\n    wait 40 ticks\n    send \"b new\"\n");
        service.reload();
        assertEquals(2, scheduler.size());
        assertEquals(0L, dispatcher.ticks());
        ticks(20);
        assertEquals(List.of("a new"), game.messages);
        ticks(20);
        assertEquals(List.of("a new", "b new"), game.messages);
        ticks(600);
        assertEquals(List.of("a new", "b new"), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void savedGlobalsAreRestoredAtStartupAndByBothVariableReloads(@TempDir Path root) throws IOException {
        new VariableStore().save(root.resolve("variables.json"), Map.of("count", 5.0));
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        start(root);
        assertEquals(Map.of("count", 5.0), variables.global());
        variables.global().put("count", 99.0);
        assertEquals(VariablesReload.RELOADED, service.reloadVariables());
        assertEquals(Map.of("count", 5.0), variables.global());
        variables.global().put("count", 99.0);
        service.reloadAll();
        assertEquals(Map.of("count", 5.0), variables.global());
    }

    @Test
    void theRememberedReportIsTheOneTheLastFullLoadReturned(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        start(root);
        assertEquals(1, service.lastReport().scriptCount());
        write(root, "b.ms", "on chat:\n    send \"b\"\n");
        LoadReport report = service.reloadAll();
        assertSame(report, service.lastReport());
        assertEquals(2, service.lastReport().scriptCount());
        write(root, "c.ms", "on chat:\n    send \"c\"\n");
        assertEquals(FileReload.Outcome.ADDED, service.reloadFile("c.ms").outcome());
        assertSame(report, service.lastReport());
    }

    @Test
    void aFullReloadForgetsTheErrorsOfAFileThatIsGone(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        write(root, "b.ms", "on chat:\n    fly\n");
        start(root);
        assertEquals(List.of("b.ms:2: unknown effect \"fly\""), errorText());
        Files.delete(root.resolve("b.ms"));
        service.reload();
        assertEquals(List.of(), service.errors());
        assertEquals(List.of("a.ms"), files());
    }

    @Test
    void aFailedParseLeavesTheRunningVersionsFramesAndLoadEventAlone(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on script load:\n    send \"a loaded\"\n    wait 50 ticks\n    send \"a done\"\n");
        write(root, "b.ms", "on script load:\n    wait 70 ticks\n    send \"b\"\n");
        start(root);
        assertEquals(List.of("a loaded"), game.messages);
        assertEquals(2, scheduler.size());
        ticks(10);
        write(root, "a.ms", "on script load:\n    fly\n");
        FileReload result = service.reloadFile("a.ms");
        assertEquals(FileReload.Outcome.KEPT, result.outcome());
        assertEquals(1, result.triggerCount());
        assertEquals(2, scheduler.size());
        assertEquals(List.of("a loaded"), game.messages);
        ticks(40);
        assertEquals(List.of("a loaded", "a done"), game.messages);
        ticks(20);
        assertEquals(List.of("a loaded", "a done", "b"), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void theErrorListIsSortedByFileWhateverOrderTheFilesArrivedIn(@TempDir Path root) throws IOException {
        write(root, "b.ms", "on chat:\n    fly\n");
        write(root, "c.ms", "on chat:\n    fly\n");
        start(root);
        assertEquals(List.of(
                "b.ms:2: unknown effect \"fly\"",
                "c.ms:2: unknown effect \"fly\""), errorText());
        write(root, "a.ms", "on chat:\n    fly\n");
        assertEquals(FileReload.Outcome.ADDED, service.reloadFile("a.ms").outcome());
        assertEquals(List.of(
                "a.ms:2: unknown effect \"fly\"",
                "b.ms:2: unknown effect \"fly\"",
                "c.ms:2: unknown effect \"fly\""), errorText());
    }

    @Test
    void aDirectoryNamedLikeAScriptIsNotAScript(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        Files.createDirectories(root.resolve("folder.ms"));
        start(root);
        assertEquals(List.of("a.ms"), files());
        assertEquals(FileReload.Outcome.MISSING, service.reloadFile("folder.ms").outcome());
        assertEquals(List.of("a.ms"), files());
        assertEquals(List.of(), service.errors());
    }

    @Test
    void everyOutcomeThatChangesNothingStillReportsTheElapsedTime(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        write(root, "b.ms", "on chat:\n    send \"b\"\n");
        start(root);
        FileReload missing = service.reloadFile("nope.ms");
        assertEquals(FileReload.Outcome.MISSING, missing.outcome());
        assertEquals(1L, missing.millis());
        assertEquals(2L, service.lastMillis());
        FileReload refused = service.reloadFile("sub/a.ms");
        assertEquals(FileReload.Outcome.REFUSED, refused.outcome());
        assertEquals(1L, refused.millis());
        assertEquals(2L, service.lastMillis());
        write(root, "a.ms", "on chat:\n    fly\n");
        FileReload kept = service.reloadFile("a.ms");
        assertEquals(FileReload.Outcome.KEPT, kept.outcome());
        assertEquals(1L, kept.millis());
        assertEquals(2L, service.lastMillis());
        Files.delete(root.resolve("b.ms"));
        FileReload removed = service.reloadFile("b.ms");
        assertEquals(FileReload.Outcome.REMOVED, removed.outcome());
        assertEquals(1L, removed.millis());
        assertEquals(2L, service.lastMillis());
    }
}
