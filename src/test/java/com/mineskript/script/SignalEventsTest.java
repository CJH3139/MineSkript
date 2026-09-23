package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameSignals;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SignalEventsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler());

    private void load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
        registry.replace(List.of(script));
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void everyNewEventParses() {
        load("on actionbar:\n    stop\non title:\n    stop\non subtitle:\n    stop\non bossbar update:\n    stop\n"
                + "on tab list change:\n    stop\non scoreboard update:\n    stop\non sound played:\n    stop\n"
                + "on particle spawn:\n    stop\non entity spawn:\n    stop\non entity despawn:\n    stop\non entity death:\n    stop\n"
                + "on player join server:\n    stop\non chunk load:\n    stop\non chunk unload:\n    stop\non client tick:\n    stop\n"
                + "on render tick:\n    stop\non key press of \"ctrl+shift+x\":\n    stop\non mouse scroll:\n    stop\non toast:\n    stop\n"
                + "on advancement:\n    stop\non server disconnect:\n    stop\non level up:\n    stop\non time change:\n    stop\n");
        assertEquals(23, registry.triggers().size());
    }

    @Test
    void signalsCarryTheirValues() {
        load("on actionbar:\n    send \"bar %event-text%\"\n"
                + "on sound:\n    send \"%event-sound% at %event-x%\"\n"
                + "on entity death:\n    send \"%event-entity% died\"\n"
                + "on disconnect:\n    set {-why} to event-reason\n");
        ticks(1);
        assertTrue(GameSignals.wants("actionbar"));
        dispatcher.onSignal(new GameSignals.Signal("actionbar", Map.of("text", "hi")));
        dispatcher.onSignal(new GameSignals.Signal("sound", Map.of("sound", "minecraft:block.note_block.harp", "x", 1.0, "y", 2.0, "z", 3.0)));
        dispatcher.onSignal(new GameSignals.Signal("entity death", Map.of("entity", new EntityValue("minecraft:zombie", "Zombie", 0, 0, 0, 1))));
        assertEquals(List.of("bar hi", "minecraft:block.note_block.harp at 1", "Zombie died"), game.messages);
        game.hasWorld = false;
        dispatcher.onSignal(new GameSignals.Signal("disconnect", Map.of("reason", "kicked")));
        game.hasWorld = true;
        dispatcher.onSignal(new GameSignals.Signal("actionbar", Map.of("text", "again")));
        assertEquals("bar again", game.messages.getLast());
    }

    @Test
    void emitOnlyQueuesEventsSomeoneListensFor() {
        load("on toast:\n    stop\n");
        ticks(1);
        GameSignals.drain();
        GameSignals.emit("particle", Map.of());
        GameSignals.emitOnce("toast");
        GameSignals.emitOnce("toast");
        assertEquals(List.of("toast"), GameSignals.drain().stream().map(GameSignals.Signal::event).toList());
    }

    @Test
    void keyComboNeedsItsModifiers() {
        load("on key press of \"ctrl+shift+x\":\n    send \"combo\"\n");
        ticks(1);
        game.keysDown.add("key.keyboard.x");
        ticks(1);
        game.keysDown.remove("key.keyboard.x");
        ticks(1);
        game.keysDown.add("key.keyboard.right.control");
        game.keysDown.add("key.keyboard.left.shift");
        game.keysDown.add("key.keyboard.x");
        ticks(1);
        assertEquals(List.of("combo"), game.messages);
    }

    @Test
    void cancellingChatSendBlocksTheMessage() {
        load("on chat send:\n    if message contains \"secret\":\n        cancel event\n");
        assertFalse(dispatcher.onChatSend("my secret"));
        assertTrue(dispatcher.onChatSend("hello"));
    }

    @Test
    void cancelOutsideChatSendIsAParseError() {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", "on chat:\n    cancel event\n");
        assertEquals(List.of("t.ms:2: only \"on chat send\" and \"on command send\" can be cancelled"),
                script.errors().stream().map(Object::toString).toList());
    }

    @Test
    void levelUpOnlyFiresGoingUp() {
        load("on level up:\n    send \"up %event-level change%\"\n");
        ticks(1);
        game.xpLevel = 2;
        ticks(1);
        game.xpLevel = 1;
        ticks(1);
        assertEquals(List.of("up 2"), game.messages);
    }

    @Test
    void timeChangeFiresOnJumpsNotNormalTicking() {
        load("on time change:\n    send \"time %event-time%\"\n");
        game.dayTime = 100;
        ticks(1);
        game.dayTime = 101;
        ticks(1);
        game.dayTime = 13000;
        ticks(1);
        assertEquals(List.of("time 13000"), game.messages);
    }

    @Test
    void clientTickAndFrameFire() {
        load("on client tick:\n    add 1 to {-ticks}\non frame:\n    add 1 to {-frames}\nevery 3 ticks:\n    send \"%{-ticks}% %{-frames}%\"\n");
        dispatcher.onFrame();
        dispatcher.onFrame();
        ticks(3);
        assertEquals(List.of("2 2"), game.messages);
    }
}
