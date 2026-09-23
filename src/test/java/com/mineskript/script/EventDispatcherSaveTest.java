package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventDispatcherSaveTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Variables variables = new Variables();
    private int saves;
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), variables, () -> saves++);

    private void load(String source) {
        registry.replace(List.of(new Parser(DefaultSyntax.registry()).parse("t.ms", source)));
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void savesSixtyTicksAfterTheLastGlobalChange() {
        load("on load:\n    set {x} to 1\n");
        dispatcher.onLoad();
        ticks(60);
        assertEquals(0, saves);
        ticks(1);
        assertEquals(1, saves);
        ticks(200);
        assertEquals(1, saves);
        dispatcher.onLoad();
        ticks(60);
        assertEquals(1, saves);
        ticks(1);
        assertEquals(2, saves);
    }

    @Test
    void ramAndLocalChangesNeverTriggerASave() {
        load("on load:\n    set {-r} to 1\n    set {_l} to 2\n");
        dispatcher.onLoad();
        ticks(200);
        assertEquals(0, saves);
    }

    @Test
    void continuousChangesDeferTheSaveUntilQuiet() {
        load("every tick:\n    add 1 to {x}\n");
        ticks(100);
        assertEquals(0, saves);
        registry.replace(List.of());
        ticks(59);
        assertEquals(0, saves);
        ticks(1);
        assertEquals(1, saves);
    }

    @Test
    void disconnectAndResetAlwaysAskToSave() {
        load("on load:\n    set {x} to 1\n");
        dispatcher.onLoad();
        dispatcher.onDisconnect();
        assertEquals(1, saves);
        ticks(100);
        assertEquals(1, saves);
        dispatcher.reset();
        assertEquals(2, saves);
    }

    @Test
    void changesMadeWithNoWorldAreSavedOnDisconnect() {
        game.hasWorld = false;
        load("on load:\n    set {x} to 1\n");
        dispatcher.onLoad();
        ticks(100);
        assertEquals(0, saves);
        dispatcher.onDisconnect();
        assertEquals(1, saves);
    }

    @Test
    void variablesFlowIntoTriggerContexts() {
        load("on load:\n    set {x} to 7\n\non load:\n    send {x}\n");
        dispatcher.onLoad();
        assertEquals(List.of("7"), game.messages);
        assertEquals(7.0, variables.global().get("x"));
    }
}
