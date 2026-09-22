package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EventDispatcherTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler);

    private void load(String source) {
        registry.replace(List.of(new Parser(DefaultSyntax.registry()).parse("t.ms", source)));
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void periodicTriggersFireOnTheirCadence() {
        load("every 3 ticks:\n    send \"tick %player's y-coordinate%\"\n");
        ticks(7);
        assertEquals(List.of("tick 64", "tick 64"), game.messages);
        assertEquals(7, dispatcher.ticks());
    }

    @Test
    void waitResumesOnTheExactTick() {
        load("every 100 ticks:\n    send \"a\"\n    wait 3 ticks\n    send \"b\"\n");
        ticks(100);
        assertEquals(List.of("a"), game.messages);
        ticks(2);
        assertEquals(List.of("a"), game.messages);
        ticks(1);
        assertEquals(List.of("a", "b"), game.messages);
        assertEquals(0, scheduler.size());
    }

    @Test
    void keyPressAndReleaseEdgeDetection() {
        load("on key press of \"r\":\n    send \"down\"\non key release of \"r\":\n    send \"up\"\n");
        assertEquals(Set.of("key.keyboard.r"), registry.watchedKeys());
        ticks(1);
        game.keysDown.add("key.keyboard.r");
        ticks(3);
        game.keysDown.remove("key.keyboard.r");
        ticks(2);
        assertEquals(List.of("down", "up"), game.messages);
    }

    @Test
    void keyAlreadyHeldAtFirstPollDoesNotFire() {
        load("on key press of \"r\":\n    send \"down\"\n");
        game.keysDown.add("key.keyboard.r");
        ticks(2);
        assertTrue(game.messages.isEmpty());
    }

    @Test
    void chatRoutesTheMessage() {
        load("on chat:\n    if message is \"ping\":\n        send \"pong\"\n");
        dispatcher.onChat("ping");
        dispatcher.onChat("other");
        assertEquals(List.of("pong"), game.messages);
    }

    @Test
    void loadFiresEvenWithoutAWorld() {
        game.hasWorld = false;
        load("on load:\n    send \"loaded\"\nevery tick:\n    send \"never\"\n");
        dispatcher.onLoad();
        ticks(3);
        dispatcher.onChat("x");
        assertEquals(List.of("loaded"), game.messages);
    }

    @Test
    void multipleTriggersRunInOrderAndReloadsReplaceThem() {
        load("every tick:\n    make player say \"x\"\nevery tick:\n    send \"still\"\n");
        game.hasWorld = true;
        ticks(1);
        assertEquals(List.of("still"), game.messages);
        assertEquals(List.of("chat:x"), game.calls);
        load("every tick:\n    send \"%player's health%\"\n    stop\nevery tick:\n    send \"after\"\n");
        ticks(1);
        assertEquals(List.of("still", "20", "after"), game.messages);
    }

    @Test
    void scriptErrorsPrintOneRedLine() {
        game.hasWorld = true;
        load("every tick:\n    send \"a\"\n    wait 1 tick\n    send \"b\"\n");
        ticks(1);
        game.hasWorld = false;
        registry.replace(List.of(new Parser(DefaultSyntax.registry()).parse("t.ms", "on load:\n    click attack\n")));
        dispatcher.onLoad();
        assertEquals(List.of("t.ms:2: no world"), game.errors);
    }

    @Test
    void disconnectAndResetReleaseKeysAndClearTheScheduler() {
        load("every tick:\n    hold attack\n    wait 50 ticks\n    release attack\n");
        ticks(1);
        assertEquals(1, scheduler.size());
        dispatcher.onDisconnect();
        assertEquals(0, scheduler.size());
        assertEquals("releaseAll", game.calls.getLast());
        ticks(1);
        dispatcher.reset();
        assertEquals(0, scheduler.size());
        assertEquals(0, dispatcher.ticks());
    }
}
