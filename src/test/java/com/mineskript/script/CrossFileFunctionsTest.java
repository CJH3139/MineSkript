package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Functions;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CrossFileFunctionsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final Functions functions = new Functions();
    private final ScriptRegistry registry = new ScriptRegistry(functions);
    private final Variables variables = new Variables();
    private EventDispatcher dispatcher;
    private ScriptService service;

    private void write(Path root, String name, String source) throws IOException {
        Files.writeString(root.resolve(name), source, StandardCharsets.UTF_8);
    }

    private void start(Path root) {
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), root.resolve("variables.json"), variables, game);
        dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), variables, () -> {
        });
        service = new ScriptService(root, new ScriptLoader(new Parser(DefaultSyntax.registry(), functions)), registry, dispatcher, persistence,
                new ConfigFile(root.resolve(Config.NAME)));
        service.start();
    }

    private List<String> errors() {
        return service.errors().stream().map(ParseError::toString).toList();
    }

    @Test
    void aFunctionInOneFileIsCallableFromAnEarlierFile(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send shout(\"hi\")\n");
        write(root, "b.ms", "function shout(t: text) :: text:\n    return uppercase {_t}\n");
        start(root);
        assertEquals(List.of(), errors());
        dispatcher.onChat("x");
        assertEquals(List.of("HI"), game.messages);
    }

    @Test
    void reloadingTheDefiningFileChangesWhatCallersRun(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    send shout(\"hi\")\n");
        write(root, "b.ms", "function shout(t: text) :: text:\n    return uppercase {_t}\n");
        start(root);
        write(root, "b.ms", "function shout(t: text) :: text:\n    return \"%{_t}%!\"\n");
        service.reloadFile("b.ms");
        dispatcher.onChat("x");
        assertEquals(List.of("hi!"), game.messages);
    }

    @Test
    void aLocalFunctionIsInvisibleToOtherFiles(@TempDir Path root) throws IOException {
        write(root, "a.ms", "on chat:\n    helper()\n");
        write(root, "b.ms", "local function helper():\n    send \"b\"\n");
        start(root);
        assertEquals(List.of("a.ms:2: unknown function \"helper\""), errors());
    }

    @Test
    void theSameGlobalNameInTwoFilesIsAnErrorInTheLaterOne(@TempDir Path root) throws IOException {
        write(root, "a.ms", "function twin():\n    send \"a\"\n");
        write(root, "b.ms", "function twin():\n    send \"b\"\n");
        start(root);
        assertEquals(List.of("b.ms:1: function \"twin\" is already defined in a.ms"), errors());
    }

    @Test
    void localFunctionsWithTheSameNameLiveSideBySide(@TempDir Path root) throws IOException {
        write(root, "a.ms", "local function me() :: text:\n    return \"a\"\non chat:\n    send me()\n");
        write(root, "b.ms", "local function me() :: text:\n    return \"b\"\non chat:\n    send me()\n");
        start(root);
        assertEquals(List.of(), errors());
        dispatcher.onChat("x");
        assertEquals(List.of("a", "b"), game.messages);
    }
}
