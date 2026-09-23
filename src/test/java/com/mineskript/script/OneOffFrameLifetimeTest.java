package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OneOffFrameLifetimeTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final Variables variables = new Variables();
    private EventDispatcher dispatcher;
    private ScriptService service;
    private EffectCommands effects;

    private void startIn(Path root) {
        ConfigFile config = new ConfigFile(root.resolve("config.txt"));
        dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler, variables, () -> {
        });
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), root.resolve("variables.json"), variables, game);
        service = new ScriptService(root, new ScriptLoader(new Parser(DefaultSyntax.registry())), registry, dispatcher, persistence, config);
        effects = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, config, game);
        service.start();
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    private void writeImpostor(Path root) throws IOException {
        Files.writeString(root.resolve(EffectCommands.FILE), "on world join:\n    send \"impostor\"\n", StandardCharsets.UTF_8);
    }

    @Test
    void aParkedOneOffFrameIsNotDroppedByASingleFileReloadOfAnotherScript(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("a.ms"), "on world join:\n    send \"a\"\n", StandardCharsets.UTF_8);
        startIn(root);
        ticks(1);
        game.sneaking = false;
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait until player is sneaking"));
        assertEquals(1, dispatcher.parkedSize());
        assertEquals(FileReload.Outcome.RELOADED, service.reloadFile("a.ms").outcome());
        assertEquals(1, dispatcher.parkedSize());
        game.sneaking = true;
        ticks(2);
        assertEquals(0, dispatcher.parkedSize());
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aParkedOneOffFrameSurvivesASingleFileReloadAimedAtItsOwnFileName(@TempDir Path root) throws IOException {
        writeImpostor(root);
        startIn(root);
        ticks(1);
        game.sneaking = false;
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait until player is sneaking"));
        assertEquals(1, dispatcher.parkedSize());
        service.reloadFile(EffectCommands.FILE);
        assertEquals(1, dispatcher.parkedSize());
        game.sneaking = true;
        ticks(2);
        assertEquals(0, dispatcher.parkedSize());
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aScheduledOneOffFrameSurvivesASingleFileReloadAimedAtItsOwnFileNameAndStillResumes(@TempDir Path root) throws IOException {
        writeImpostor(root);
        startIn(root);
        ticks(1);
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait 20 ticks"));
        assertEquals(1, scheduler.size());
        service.reloadFile(EffectCommands.FILE);
        assertEquals(1, scheduler.size());
        ticks(19);
        assertEquals(1, scheduler.size());
        ticks(2);
        assertEquals(0, scheduler.size());
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aParkedOneOffFrameIsDiscardedByAFullReload(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("a.ms"), "on world join:\n    send \"a\"\n", StandardCharsets.UTF_8);
        startIn(root);
        ticks(1);
        game.sneaking = false;
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait until player is sneaking"));
        assertEquals(1, dispatcher.parkedSize());
        service.reloadAll();
        assertEquals(0, dispatcher.parkedSize());
        game.sneaking = true;
        ticks(2);
        assertEquals(0, dispatcher.parkedSize());
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aScheduledOneOffFrameIsDiscardedByAFullReload(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("a.ms"), "on world join:\n    send \"a\"\n", StandardCharsets.UTF_8);
        startIn(root);
        ticks(1);
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait 20 ticks"));
        assertEquals(1, scheduler.size());
        service.reloadAll();
        assertEquals(0, scheduler.size());
        ticks(22);
        assertEquals(0, scheduler.size());
        assertEquals(List.of(), game.errors);
    }
}
