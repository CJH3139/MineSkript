package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

class ClosingFixTest {
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
    void aSecondTriggerOnTheSameStateEventDoesNotRunWithoutAWorld() {
        Reentrant world = new Reentrant((bridge, ignored) -> bridge.game.hasWorld = false);
        assertEquals(List.of(), world.load("""
                on sneak:
                    disconnect

                on sneak:
                    send "%biome%"
                """));
        world.ticks(1);
        world.game.sneaking = true;
        world.ticks(1);
        assertEquals(List.of(), world.game.messages);
        assertEquals(List.of(), world.game.errors);
    }

    @Test
    void aSecondLoadTriggerDoesNotRunWithoutAWorld() {
        Reentrant world = new Reentrant((bridge, ignored) -> bridge.game.hasWorld = false);
        assertEquals(List.of(), world.load("""
                on load:
                    disconnect

                on load:
                    send "%biome%"
                """));
        world.dispatcher.onLoad();
        assertEquals(List.of(), world.game.messages);
        assertEquals(List.of(), world.game.errors);
    }

    @Test
    void aWaitUntilReachedAfterAReloadInTheSameRunIsDropped() {
        Reentrant world = new Reentrant((bridge, command) -> {
            bridge.dispatcher.reset();
            bridge.registry.replace(List.of());
        });
        assertEquals(List.of(), world.load("""
                on world join:
                    execute command "mineskript reload"
                    wait until player is sprinting
                    send "stale"
                """));
        world.ticks(1);
        world.game.sprinting = true;
        world.ticks(10);
        assertEquals(List.of(), world.game.messages);
        assertEquals(List.of(), world.game.errors);
    }

    @Test
    void aPlainWaitReachedAfterAReloadInTheSameRunIsDropped() {
        Reentrant world = new Reentrant((bridge, command) -> {
            bridge.dispatcher.reset();
            bridge.registry.replace(List.of());
        });
        assertEquals(List.of(), world.load("""
                on world join:
                    execute command "mineskript reload"
                    wait 1 tick
                    send "stale"
                """));
        world.ticks(10);
        assertEquals(List.of(), world.game.messages);
        assertEquals(List.of(), world.game.errors);
    }

    @Test
    void aSecondTabListArrivalInTheSameSessionFiresItsJoins() {
        assertEquals(List.of(), load("on player join:\n    send \"+%event-player%\"\non player leave:\n    send \"-%event-player%\"\n"));
        ticks(1);
        game.onlineNames.add("Alex");
        ticks(1);
        assertEquals(List.of(), game.messages);
        game.onlineNames.clear();
        ticks(1);
        assertEquals(List.of("-Alex"), game.messages);
        game.onlineNames.add("Steve");
        ticks(1);
        assertEquals(List.of("-Alex", "+Steve"), game.messages);
        game.onlineNames.clear();
        ticks(1);
        game.hasWorld = false;
        ticks(1);
        game.hasWorld = true;
        ticks(1);
        game.onlineNames.add("Zoe");
        ticks(1);
        assertEquals(List.of("-Alex", "+Steve", "-Steve"), game.messages);
    }

    @Test
    void aPlainWaitScheduledOnAWorldlessTickIsDropped() {
        assertEquals(List.of(), load("on world leave:\n    wait 5 seconds\n    send \"back\"\n"));
        ticks(1);
        game.hasWorld = false;
        ticks(1);
        game.hasWorld = true;
        ticks(200);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    private static final class Reentrant {
        private final FakeGameBridge game = new FakeGameBridge();
        private final ScriptRegistry registry = new ScriptRegistry();
        private final Variables variables = new Variables();
        private final EventDispatcher dispatcher;

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
