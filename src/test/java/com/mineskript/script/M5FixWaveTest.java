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
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class M5FixWaveTest {
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

    private static List<String> text(List<MessageLine> lines) {
        return lines.stream().map(line -> line.kind() + " " + line.text()).toList();
    }

    private static List<String> strings(List<ParseError> errors) {
        return errors.stream().map(Object::toString).toList();
    }

    @Test
    void aReloadTypedInTheWrongCaseReplacesTheRunningScriptInsteadOfAddingASecondCopy(@TempDir Path root) throws IOException {
        write(root, "Mining.ms", "on script load:\n    wait 30 ticks\n    send \"old\"\n");
        Assumptions.assumeTrue(Files.isRegularFile(root.resolve("mining.ms")));
        start(root);
        assertEquals(1, scheduler.size());
        ticks(10);
        write(root, "Mining.ms", "on script load:\n    wait 30 ticks\n    send \"new\"\n");
        FileReload result = service.reloadFile("mining.ms");
        assertEquals(FileReload.Outcome.RELOADED, result.outcome());
        assertEquals("Mining.ms", result.file());
        assertEquals(List.of("Mining.ms"), files());
        assertEquals(1, registry.triggers().size());
        assertEquals("Mining.ms", registry.triggers().get(0).file());
        assertEquals(1, scheduler.size());
        ticks(60);
        assertEquals(List.of("new"), game.messages);
    }

    @Test
    void aBrokenFileReloadedInTheWrongCaseStillKeepsTheVersionAlreadyRunning(@TempDir Path root) throws IOException {
        write(root, "Mining.ms", "on chat:\n    send \"old\"\n");
        Assumptions.assumeTrue(Files.isRegularFile(root.resolve("mining.ms")));
        start(root);
        write(root, "Mining.ms", "on chat:\n    fly\n");
        FileReload result = service.reloadFile("mining.ms");
        assertEquals(FileReload.Outcome.KEPT, result.outcome());
        assertEquals("Mining.ms", result.file());
        assertEquals(List.of("Mining.ms"), files());
        dispatcher.onChat("hi");
        assertEquals(List.of("old"), game.messages);
        assertEquals(List.of("Mining.ms:2: unknown effect \"fly\""), strings(service.errors()));
    }

    @Test
    void aNameIsResolvedToTheSpellingOnDiskAndNeverOutOfTheFolder(@TempDir Path root) throws IOException {
        write(root, "Chat.ms", "on chat:\n    send \"a\"\n");
        start(root);
        assertEquals("Chat.ms", service.canonical("Chat.ms"));
        assertEquals("nope.ms", service.canonical("nope.ms"));
        assertEquals("../secret.ms", service.canonical("../secret.ms"));
        assertEquals("..\\secret.ms", service.canonical("..\\secret.ms"));
        assertEquals("notes.txt", service.canonical("notes.txt"));
        assertEquals("Chat.MS", service.canonical("Chat.MS"));
        assertEquals(FileReload.Outcome.REFUSED, service.reloadFile("Chat.MS").outcome());
    }

    @Test
    void aDeletedScriptKeepsItsSpellingSoItCanBeUnloadedByName(@TempDir Path root) throws IOException {
        write(root, "Chat.ms", "on chat:\n    send \"a\"\n");
        start(root);
        Files.delete(root.resolve("Chat.ms"));
        assertEquals("Chat.ms", service.canonical("chat.ms"));
        assertEquals(FileReload.Outcome.REMOVED, service.reloadFile("chat.ms").outcome());
        assertEquals(List.of(), files());
    }

    @Test
    void completionOffersAScriptThatIsStillLoadedAfterItsFileIsDeleted(@TempDir Path root) throws IOException {
        write(root, "Chat.ms", "on chat:\n    send \"a\"\n");
        write(root, "mining.ms", "on chat:\n    send \"b\"\n");
        start(root);
        Files.delete(root.resolve("Chat.ms"));
        assertEquals(List.of("mining.ms"), ScriptNames.list(root));
        assertEquals(List.of("Chat.ms", "mining.ms"), ScriptNames.matching(root, service.loadedNames(), ""));
        assertEquals(List.of("Chat.ms"), ScriptNames.matching(root, service.loadedNames(), "c"));
        assertEquals(List.of("mining.ms"), ScriptNames.matching(root, ""));
    }

    @Test
    void aFullReloadDoesNotPutTheExampleBackWhenEveryScriptIsDeleted(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        start(root);
        assertEquals(List.of("a.ms"), files());
        Files.delete(root.resolve("a.ms"));
        LoadReport report = service.reload();
        assertEquals(0, report.scriptCount());
        assertEquals(List.of(), files());
        assertFalse(Files.exists(root.resolve("example.ms")));
    }

    @Test
    void theExampleIsWrittenOnlyWhenTheModFirstCreatesTheFolder(@TempDir Path root) throws IOException {
        Path dir = root.resolve("mineskript");
        assertTrue(ScriptLoader.createFolder(dir));
        assertTrue(Files.exists(dir.resolve("example.ms")));
        Files.delete(dir.resolve("example.ms"));
        assertFalse(ScriptLoader.createFolder(dir));
        assertFalse(Files.exists(dir.resolve("example.ms")));
    }

    @Test
    void theFirstStartOnAMissingFolderStillGetsTheExample(@TempDir Path root) {
        Path dir = root.resolve("mineskript");
        start(dir);
        assertEquals(List.of("example.ms"), files());
    }

    @Test
    void aVariablesReloadThatCouldNotReadTheFileSaysSoInsteadOfClaimingSuccess(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        new VariableStore().save(root.resolve("variables.json"), Map.of("count", 5.0));
        start(root);
        assertEquals(VariablesReload.RELOADED, service.reloadVariables());
        Files.writeString(root.resolve("variables.json"), "{ not json at all", StandardCharsets.UTF_8);
        assertEquals(VariablesReload.UNREADABLE, service.reloadVariables());
        assertEquals(List.of("ERROR did not re-read variables.json, it could not be read (1 ms)"),
                text(Messages.variablesReloaded(root.resolve("variables.json"), VariablesReload.UNREADABLE, 1)));
    }

    @Test
    void listReportsTheErrorsTheErrorCommandReportsAfterAKeptReload(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"a\"\non load:\n    send \"b\"\n");
        start(root);
        assertEquals(List.of("INFO a.ms, 2 triggers"), text(Messages.list(root, registry.scripts(), service.errors())).subList(1, 2));
        write(root, "a.ms", "on chat:\n    fly\n");
        assertEquals(FileReload.Outcome.KEPT, service.reloadFile("a.ms").outcome());
        assertEquals(List.of("a.ms:2: unknown effect \"fly\""), strings(service.errors()));
        assertEquals(List.of(
                "SUCCESS 1 script loaded from " + root,
                "WARNING a.ms, 2 triggers, 1 error"), text(Messages.list(root, registry.scripts(), service.errors())));
    }

    @Test
    void anErrorCarriesTheSourceLineItWasParsedFromNotTheLineThereNow(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    fly\n");
        start(root);
        assertEquals(List.of("a.ms:2: unknown effect \"fly\""), strings(service.errors()));
        write(root, "a.ms", "on chat:\n    send \"repaired but not reloaded\"\n");
        assertEquals("fly", service.sources().line("a.ms", 2));
        assertEquals(List.of(
                "ERROR 1 error from the last load",
                "ERROR a.ms:2: unknown effect \"fly\"",
                "DETAIL     fly"), text(Messages.errors("the last load", service.errors(), service.sources())));
        Files.delete(root.resolve("a.ms"));
        assertEquals("fly", service.sources().line("a.ms", 2));
    }

    @Test
    void aKeptReloadShowsTheSourceLineOfTheTextThatFailedToParse(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send \"old\"\n");
        start(root);
        write(root, "a.ms", "on chat:\n    hold attak\n");
        FileReload result = service.reloadFile("a.ms");
        assertEquals(FileReload.Outcome.KEPT, result.outcome());
        assertEquals(List.of(
                "ERROR 1 error from a.ms",
                "ERROR a.ms:2: unknown effect \"hold attak\"",
                "DETAIL     hold attak"), text(Messages.errors(result.file(), result.errors(), service.sources())));
    }

    @Test
    void theErrorListIsSortedByFileWhicheverCommandAsksForIt(@TempDir Path root) throws IOException {
        write(root, "b.ms", "on chat:\n    fly\n");
        start(root);
        write(root, "a.ms", "on chat:\n    fly\n");
        assertEquals(FileReload.Outcome.ADDED, service.reloadFile("a.ms").outcome());
        assertEquals(List.of("a.ms:2: unknown effect \"fly\"", "b.ms:2: unknown effect \"fly\""), strings(service.errors()));
        LoadReport report = service.reload();
        assertEquals(strings(report.errors()), strings(service.errors()));
    }

    @Test
    void theAliasIsTheOnlyInputThatIsPassedOnToTheServer() {
        assertTrue(Messages.typedTheAlias("ms"));
        assertTrue(Messages.typedTheAlias("ms warp home"));
        assertFalse(Messages.typedTheAlias("mineskript"));
        assertFalse(Messages.typedTheAlias("mineskript warp home"));
        assertFalse(Messages.typedTheAlias("msg someone hello"));
        assertEquals(List.of("ERROR not a MineSkript command, type /mineskript help for the tree"),
                text(Messages.unknownBranch()));
    }

    @Test
    void aBlockOfLinesIsPrefixedOnceAndIndentedUnderneath() {
        assertEquals(Messages.PREFIX.length() + 1, Messages.INDENT.length());
        assertEquals(" ".repeat(Messages.INDENT.length()), Messages.INDENT);
    }
}
