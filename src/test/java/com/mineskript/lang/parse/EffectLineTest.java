package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EffectLineTest {
    private final Parser parser = new Parser(DefaultSyntax.registry());

    private ParsedEffect parse(String text) {
        return parser.parseEffect("effect command", 0, new Event.EffectCommand(), text);
    }

    private String error(String text) {
        ParsedEffect parsed = parse(text);
        assertTrue(parsed.failed(), "expected a parse error from: " + text);
        return parsed.error().toString();
    }

    @Test
    void oneGoodEffectParsesAndRuns() {
        ParsedEffect parsed = parse("send \"hello\"");
        assertFalse(parsed.failed());
        assertNull(parsed.error());
        FakeGameBridge game = new FakeGameBridge();
        parsed.statement().execute(new Context(game, "effect command", Map.of(), new Variables()));
        assertEquals(java.util.List.of("hello"), game.messages);
    }

    @Test
    void theLineIsStrippedBeforeItIsParsed() {
        assertFalse(parse("   send \"hi\"   ").failed());
        assertEquals("effect command: unknown effect \"frobnicate the widget\"", error("   frobnicate the widget   "));
    }

    @Test
    void anEmptyLineIsAnError() {
        assertEquals("effect command: there is nothing here to run", error("   "));
    }

    @Test
    void anUnknownEffectIsAnErrorInTheUsualShape() {
        assertEquals("effect command: unknown effect \"frobnicate the widget\"", error("frobnicate the widget"));
    }

    @Test
    void anUnterminatedStringIsAnErrorInTheUsualShape() {
        assertEquals("effect command: unterminated string", error("send \"oops"));
    }

    @Test
    void anEventValueIsRefusedWithAMessageThatSaysWhy() {
        assertEquals("effect command: event-damage needs an event, and an effect command typed in chat has none",
                error("send \"%event-damage%\""));
        assertEquals("effect command: event-item needs an event, and an effect command typed in chat has none",
                error("send \"%event-item%\""));
    }

    @Test
    void theMessageExpressionIsStillRefusedByItsOwnWording() {
        assertEquals("effect command: \"message\" is only available inside \"on chat\", \"on chat send\" and \"on command send\"",
                error("send message"));
    }

    @Test
    void loopOnlyEffectsAreRefusedBecauseTheScopeHasNoLoop() {
        assertEquals("effect command: continue is only available inside a loop", error("continue"));
        assertEquals("effect command: exit loop is only available inside a loop", error("exit loop"));
    }

    @Test
    void waitParsesSoAOneOffEffectCanPark() {
        ParsedEffect parsed = parse("wait 5 seconds");
        assertFalse(parsed.failed());
        Flow flow = parsed.statement().execute(new Context(new FakeGameBridge(), "effect command", Map.of(), new Variables()));
        assertEquals(new Flow.Wait(100), flow);
    }

    @Test
    void waitUntilParsesThroughTheSameSpecialCaseAScriptLineUses() {
        ParsedEffect parsed = parse("wait until player is sneaking");
        assertFalse(parsed.failed());
        assertTrue(parsed.statement() instanceof WaitUntil);
    }

    @Test
    void aSectionHeadIsNotAnEffectAndIsRefused() {
        assertEquals("effect command: unknown effect \"if player is sneaking:\"", error("if player is sneaking:"));
    }

    @Test
    void oneParserGivesEachScopeItsOwnAnswerHoweverTheTwoAreInterleaved() {
        String first = error("send \"%event-damage%\"");
        assertEquals("effect command: event-damage needs an event, and an effect command typed in chat has none", first);
        assertEquals("t.ms:2: event-damage is not available in this event",
                parser.parse("t.ms", "on load:\n    send \"%event-damage%\"\n").errors().get(0).toString());
        assertEquals(first, error("send \"%event-damage%\""));
        assertEquals("t.ms:2: event-damage is not available in this event",
                parser.parse("t.ms", "on load:\n    send \"%event-damage%\"\n").errors().get(0).toString());
    }

    @Test
    void anEventValueStillGivesItsOriginalWordingInsideARealEvent() {
        assertEquals("t.ms:2: event-damage is not available in this event",
                parser.parse("t.ms", "on load:\n    send \"%event-damage%\"\n").errors().get(0).toString());
    }
}
