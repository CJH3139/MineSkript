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
        write(root, "Mining.ms", "on chat:\n    send \"old\"\n");
        start(root);
        assertEquals(List.of("Mining.ms"), files());
        rename(root, "Mining.ms", "mining.ms", "on chat:\n    send \"new\"\n");
        assertEquals(List.of("mining.ms"), ScriptNames.list(root));
        FileReload result = service.reloadFile("mining.ms");
        assertEquals(FileReload.Outcome.RELOADED, result.outcome());
        assertEquals("mining.ms", result.file());
        assertEquals(List.of("mining.ms"), files());
        assertEquals(1, registry.triggers().size());
        assertEquals("mining.ms", registry.triggers().get(0).file());
        dispatcher.onChat("hi");
        assertEquals(List.of("new"), game.messages);
    }

    @Test
    void aCaseOnlyRenameDropsTheWaitingFramesOfTheVersionItReplaces(@TempDir Path root) throws IOException {
        write(root, "Mining.ms", "on chat:\n    wait 30 ticks\n    send \"old\"\n");
        start(root);
        dispatcher.onChat("hi");
        ticks(10);
        assertEquals(1, scheduler.size());
        rename(root, "Mining.ms", "mining.ms", "on chat:\n    send \"new\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("mining.ms").outcome());
        assertEquals(0, scheduler.size());
        ticks(60);
        assertEquals(List.of(), game.messages);
        dispatcher.onChat("hi");
        assertEquals(List.of("new"), game.messages);
    }

    @Test
    void aCaseOnlyRenameClearsTheErrorsFiledUnderTheOldSpelling(@TempDir Path root) throws IOException {
        write(root, "Mining.ms", "on chat:\n    fly\n");
        start(root);
        assertEquals(List.of("Mining.ms:2: unknown effect \"fly\""), strings(service.errors()));
        rename(root, "Mining.ms", "mining.ms", "on chat:\n    send \"new\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("mining.ms").outcome());
        assertEquals(List.of(), strings(service.errors()));
    }

    @Test
    void aWrongCaseReloadFilesItsErrorsUnderTheNameTheNextReloadClears(@TempDir Path root) throws IOException {
        write(root, "Mining.ms", "on chat:\n    send \"old\"\n");
        start(root);
        write(root, "Mining.ms", "on chat:\n    fly\n");
        FileReload kept = service.reloadFile("mining.ms");
        assertEquals(FileReload.Outcome.KEPT, kept.outcome());
        assertEquals("Mining.ms", kept.file());
        assertEquals(List.of("Mining.ms:2: unknown effect \"fly\""), strings(service.errors()));
        write(root, "Mining.ms", "on chat:\n    send \"new\"\n");
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("mining.ms").outcome());
        assertEquals(List.of(), strings(service.errors()));
    }

    @Test
    void anExactNameWinsOverACaseDifferingOneWhenAFolderHoldsBoth(@TempDir Path root) throws IOException {
        List<String> both = List.of("Mining.ms", "mining.ms");
        assertEquals(Optional.of("mining.ms"), ScriptNames.pick(both, "mining.ms"));
        assertEquals(Optional.of("Mining.ms"), ScriptNames.pick(both, "Mining.ms"));
        assertEquals(Optional.of("Mining.ms"), ScriptNames.pick(List.of("Mining.ms"), "MINING.ms"));
        assertEquals(Optional.empty(), ScriptNames.pick(both, "chat.ms"));
        write(root, "Chat.ms", "on chat:\n    send \"a\"\n");
        assertEquals(Optional.of("Chat.ms"), ScriptNames.onDisk(root, "chat.ms"));
        assertEquals(Optional.of("Chat.ms"), ScriptNames.onDisk(root, "Chat.ms"));
    }

    @Test
    void aNameOnlyTheFilesystemCanResolveIsMissingRatherThanASecondCopy(@TempDir Path root) throws IOException {
        write(root, "my-mining-script.ms", "on chat:\n    send \"a\"\n");
        start(root);
        String alias = "my-min~1.ms";
        Assumptions.assumeTrue(Files.isRegularFile(root.resolve(alias)));
        assertEquals(List.of("my-mining-script.ms"), ScriptNames.list(root));
        FileReload result = service.reloadFile(alias);
        assertEquals(FileReload.Outcome.MISSING, result.outcome());
        assertEquals(List.of("my-mining-script.ms"), files());
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
        write(root, "a.ms", "on chat:\n    send \"a\"\n");
        ScriptNames.Listing one = ScriptNames.listing(root);
        assertTrue(one.readable());
        assertEquals(List.of("a.ms"), one.names());
        ScriptNames.Listing gone = ScriptNames.listing(root.resolve("nope"));
        assertFalse(gone.readable());
        assertEquals(List.of(), gone.names());
        assertFalse(ScriptNames.listing(root.resolve("a.ms")).readable());
    }

    @Test
    void aFullLoadForgetsTheSourceLinesOfScriptsThatAreGone(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    fly\n");
        start(root);
        assertEquals("fly", service.sources().line("a.ms", 2));
        Files.delete(root.resolve("a.ms"));
        service.reload();
        assertEquals(List.of(), files());
        assertEquals("", service.sources().line("a.ms", 2));
    }
}
