package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class WaitUntilTest {
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
    void anAlreadyTrueConditionDoesNotPauseAtAll() {
        game.sneaking = true;
        load("on world join:\n    send \"before\"\n    wait until player is sneaking\n    send \"after\"\n");
        ticks(1);
        assertEquals(List.of("before", "after"), game.messages);
    }

    @Test
    void aFalseConditionParksAndResumesOnTheTickItBecomesTrue() {
        load("on world join:\n    send \"before\"\n    wait until player is sneaking\n    send \"after\"\n");
        ticks(1);
        assertEquals(List.of("before"), game.messages);
        ticks(3);
        assertEquals(List.of("before"), game.messages);
        game.sneaking = true;
        ticks(1);
        assertEquals(List.of("before", "after"), game.messages);
    }

    @Test
    void aConditionFlippedLaterInTheParkingTickIsNotSeenUntilTheNextTick() {
        assertEquals(List.of(), load("on world join:\n    wait until {flag} is 1\n    send \"resumed\"\n\non world join:\n    set {flag} to 1\n"));
        ticks(1);
        assertEquals(List.of(), game.messages);
        ticks(1);
        assertEquals(List.of("resumed"), game.messages);
    }

    @Test
    void aParkedFrameTimesOutAfterThirtySecondsWithAScriptError() {
        load("on world join:\n    send \"before\"\n    wait until player is sneaking\n    send \"after\"\n");
        ticks(1);
        ticks(600);
        assertEquals(List.of("before"), game.messages);
        assertEquals(List.of("t.ms:3: wait until timed out after 30 seconds"), game.errors);
        game.sneaking = true;
        ticks(5);
        assertEquals(List.of("before"), game.messages);
    }

    @Test
    void theLastRetestHappensExactlyAtTheDeadline() {
        load("on world join:\n    wait until player is sneaking\n    send \"after\"\n");
        ticks(1);
        ticks(599);
        assertEquals(List.of(), game.errors);
        game.sneaking = true;
        ticks(1);
        assertEquals(List.of("after"), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void reEnteringTheEventParksASecondIndependentFrame() {
        load("on sneak:\n    send \"start\"\n    wait until player is sprinting\n    send \"done\"\n");
        ticks(1);
        game.sneaking = true;
        ticks(1);
        game.sneaking = false;
        ticks(1);
        game.sneaking = true;
        ticks(1);
        assertEquals(List.of("start", "start"), game.messages);
        game.sprinting = true;
        ticks(1);
        assertEquals(List.of("start", "start", "done", "done"), game.messages);
    }

    @Test
    void waitUntilWorksInsideALoop() {
        load("on world join:\n    loop 2 times:\n        wait until player is sneaking\n        send \"round %loop-iteration%\"\n");
        ticks(1);
        assertEquals(List.of(), game.messages);
        game.sneaking = true;
        ticks(1);
        assertEquals(List.of("round 1", "round 2"), game.messages);
    }

    @Test
    void leavingTheWorldDropsEveryParkedFrame() {
        assertEquals(List.of(), load("on world join:\n    wait until player is sneaking\n    send \"after\"\n"));
        ticks(1);
        dispatcher.onDisconnect();
        game.sneaking = true;
        ticks(5);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aConditionThatThrowsAtRuntimeBecomesAScriptErrorAndUnparksTheFrame() {
        assertEquals(List.of(), load("on world join:\n    wait until block below player is stone\n    send \"after\"\n"));
        ticks(1);
        assertEquals(List.of(), game.errors);
        game.setBlock(0, -1, 0, null);
        ticks(1);
        assertEquals(List.of(), game.messages);
        assertEquals(1, game.errors.size());
        assertTrue(game.errors.get(0).startsWith("t.ms:2: "));
        ticks(5);
        assertEquals(1, game.errors.size());
    }

    @Test
    void haltUntilIsTheSameEffect() {
        game.sneaking = true;
        load("on world join:\n    halt until player is sneaking\n    send \"after\"\n");
        ticks(1);
        assertEquals(List.of("after"), game.messages);
    }

    @Test
    void anUnparsableConditionIsAParseErrorNamingTheLine() {
        List<String> errors = new Parser(DefaultSyntax.registry()).parse("t.ms", "on load:\n    wait until banana\n")
                .errors().stream().map(Object::toString).toList();
        assertEquals(List.of("t.ms:2: unknown condition \"banana\""), errors);
    }
}
