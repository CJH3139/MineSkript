package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FinalFixWaveTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
    });

    private List<String> load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        registry.replace(List.of(script));
        return script.errors().stream().map(Object::toString).toList();
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void aResetFromInsideTheResumeLoopDropsTheFramesBehindIt() {
        Reentrant world = new Reentrant((bridge, command) -> {
            bridge.dispatcher.reset();
            bridge.registry.replace(List.of());
        });
        assertEquals(List.of(), world.load("""
                on world join:
                    wait until player is sneaking
                    execute command "mineskript reload"

                on world join:
                    wait until player is sprinting
                    send "stale"
                """));
        world.ticks(1);
        world.game.sneaking = true;
        world.ticks(1);
        assertEquals(List.of("command:mineskript reload"), commands(world.game));
        world.game.sprinting = true;
        world.ticks(10);
        assertEquals(List.of(), world.game.messages);
        assertEquals(List.of(), world.game.errors);
    }

    @Test
    void survivorsStillResumeWhenNothingClearsTheParkedList() {
        assertEquals(List.of(), load("""
                on world join:
                    wait until player is sneaking
                    send "one"

                on world join:
                    wait until player is sprinting
                    send "two"
                """));
        ticks(1);
        game.sneaking = true;
        ticks(1);
        assertEquals(List.of("one"), game.messages);
        game.sprinting = true;
        ticks(1);
        assertEquals(List.of("one", "two"), game.messages);
    }

    @Test
    void aStatementAfterDisconnectDoesNotRun() {
        assertEquals(List.of(), load("on world join:\n    disconnect\n    send \"after\"\n"));
        ticks(1);
        assertEquals(List.of("disconnect"), game.calls);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void theRestOfTheTickStopsOnceTheWorldHasGone() {
        Reentrant world = new Reentrant((bridge, ignored) -> bridge.game.hasWorld = false);
        assertEquals(List.of(), world.load("""
                on key press of "g":
                    disconnect

                on key press of "g":
                    send "second"
                """));
        world.ticks(1);
        world.game.keysDown.add("key.keyboard.g");
        world.ticks(1);
        assertEquals(List.of(), world.game.messages);
        assertEquals(List.of(), world.game.errors);
    }

    @Test
    void theTabListIsOnlyReadWhenALoadedTriggerNeedsIt() {
        load("on move:\n    send \"moved\"\n");
        ticks(2);
        assertEquals(0, game.onlineNameScans);
        load("on player join:\n    send \"joined\"\n");
        ticks(1);
        assertEquals(1, game.onlineNameScans);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void aComponentTheGateTurnsOnIsBaselinedRatherThanFiredAllAtOnce() {
        load("on move:\n    send \"moved\"\n");
        game.effects.put("minecraft:speed", 1);
        game.vehicle = new EntityValue("minecraft:horse", "Horse", 0, 0, 0, 0);
        game.dimension = "minecraft:the_nether";
        game.totalExperience = 350;
        ticks(2);
        assertEquals(List.of(), load("""
                on effect gain:
                    send "effect"
                on mount:
                    send "mount"
                on dimension change:
                    send "dim"
                on xp change:
                    send "xp"
                """));
        ticks(1);
        assertEquals(List.of(), game.messages);
        game.effects.put("minecraft:haste", 1);
        ticks(1);
        assertEquals(List.of("effect"), game.messages);
    }

    @Test
    void aScriptThatReloadsItselfDoesNotRecurse(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("self.ms"), "on script load:\n    send command \"/mineskript reload\"\n");
        Reentrant world = new Reentrant((bridge, command) -> bridge.service.reload());
        world.serve(dir);
        LoadReport report = world.service.reload();
        assertEquals(1, report.scriptCount());
        assertEquals(1, report.triggerCount());
        assertEquals(List.of("command:mineskript reload"), commands(world.game));
    }

    @Test
    void aFrameParkedOnAWorldlessTickIsDropped() {
        assertEquals(List.of(), load("on world leave:\n    wait until key \"g\" is held\n    send \"back\"\n"));
        ticks(1);
        game.hasWorld = false;
        ticks(1);
        game.hasWorld = true;
        game.keysDown.add("key.keyboard.g");
        ticks(5);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aTabListArrivingLateIsABaselineRatherThanAHundredJoins() {
        assertEquals(List.of(), load("on player join:\n    send \"+%event-player%\"\non player leave:\n    send \"-%event-player%\"\n"));
        ticks(1);
        game.onlineNames.add("Alex");
        game.onlineNames.add("Steve");
        ticks(1);
        assertEquals(List.of(), game.messages);
        game.onlineNames.add("Zoe");
        ticks(1);
        assertEquals(List.of("+Zoe"), game.messages);
        game.onlineNames.clear();
        ticks(1);
        assertEquals(List.of("+Zoe", "-Alex", "-Steve", "-Zoe"), game.messages);
    }

    private static List<String> commands(FakeGameBridge bridge) {
        return bridge.calls.stream().filter(call -> call.startsWith("command:")).toList();
    }

    private static final class Reentrant {
        private final FakeGameBridge game = new FakeGameBridge();
        private final ScriptRegistry registry = new ScriptRegistry();
        private final Variables variables = new Variables();
        private EventDispatcher dispatcher;
        private ScriptService service;

        private Reentrant(BiConsumer<Reentrant, String> onCommand) {
            GameBridge bridge = (GameBridge) Proxy.newProxyInstance(GameBridge.class.getClassLoader(),
                    new Class<?>[] {GameBridge.class}, (proxy, method, args) -> invoke(method, args, onCommand));
            dispatcher = new EventDispatcher(registry, bridge, new Interpreter(10_000), new Scheduler(), variables, () -> {
            });
        }

        private Object invoke(Method method, Object[] args, BiConsumer<Reentrant, String> onCommand) throws Throwable {
            Object result;
            try {
                result = method.invoke(game, args);
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
            if (method.getName().equals("sendCommand") || method.getName().equals("disconnect")) {
                onCommand.accept(this, method.getName().equals("sendCommand") ? (String) args[0] : "");
            }
            return result;
        }

        private void serve(Path dir) {
            service = new ScriptService(dir, new ScriptLoader(new Parser(DefaultSyntax.registry())), registry, dispatcher,
                    new VariablePersistence(new VariableStore(), dir.resolve("variables.json"), variables, game),
                    new ConfigFile(dir.resolve(Config.NAME)));
        }

        private List<String> load(String source) {
            ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
            registry.replace(List.of(script));
            return script.errors().stream().map(Object::toString).toList();
        }

        private void ticks(int count) {
            for (int i = 0; i < count; i++) {
                dispatcher.tick();
            }
        }
    }
}
