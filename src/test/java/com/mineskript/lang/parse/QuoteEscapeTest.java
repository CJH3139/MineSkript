package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.lexer.LexResult;
import com.mineskript.lang.lexer.Lexer;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuoteEscapeTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void doubledQuoteInsideTextIsOneQuote() {
        runner.run("on load:\n    send \"she said \"\"hi\"\"\"\n");
        assertEquals(List.of("she said \"hi\""), runner.game.messages);
    }

    @Test
    void doubledQuotesAtTheEdgesAndAlone() {
        runner.run("on load:\n    send \"\"\"quoted\"\"\"\n    send \"[\"\"]\"\n    set {_e} to \"\"\n    send \"(%{_e}%)\"\n");
        assertEquals(List.of("\"quoted\"", "[\"]", "()"), runner.game.messages);
    }

    @Test
    void tokenizerUnescapesDoubledQuotes() {
        assertEquals(List.of(new Token("send", false), new Token("a \"b\" c", true)), Tokenizer.tokenize("send \"a \"\"b\"\" c\""));
        assertThrows(TokenizeException.class, () -> Tokenizer.tokenize("send \"a \"\"b\"\""));
    }

    @Test
    void hashInsideEscapedTextIsNotAComment() {
        runner.run("on load:\n    send \"x \"\"#\"\" y\" # a real comment\n");
        assertEquals(List.of("x \"#\" y"), runner.game.messages);
    }

    @Test
    void lexerKeepsTheWholeLineAroundEscapedQuotes() {
        LexResult result = Lexer.lex("t.ms", "on load:\n    send \"a\"\"#\"\"b\"  # c\n");
        assertEquals("send \"a\"\"#\"\"b\"", result.nodes().get(0).children().get(0).text());
    }

    @Test
    void escapedQuotesMixWithInterpolation() {
        runner.run("on load:\n    set {_n} to \"Alex\"\n    send \"%{_n}% said \"\"hi\"\" at %1 + 1%\"\n");
        assertEquals(List.of("Alex said \"hi\" at 2"), runner.game.messages);
    }

    @Test
    void textInsideAnInterpolationIsItsOwnString() {
        runner.run("on load:\n    send \"[%\"a\" joined with \"b\"%]\" # comment\n");
        assertEquals(List.of("[ab]"), runner.game.messages);
    }

    @Test
    void hashInsideTextInsideAnInterpolationIsNotAComment() {
        runner.run("on load:\n    send \"%\"#1\" joined with \"!\"%\"\n");
        assertEquals(List.of("#1!"), runner.game.messages);
    }

    @Test
    void doublePercentIsStillOnePercent() {
        runner.run("on load:\n    send \"100%% sure, \"\"really\"\"\"\n");
        assertEquals(List.of("100% sure, \"really\""), runner.game.messages);
    }

    @Test
    void escapedQuotesWorkInConditionsAndComparisons() {
        runner.run("on load:\n    set {_q} to \"say \"\"x\"\"\"\n    if {_q} is \"say \"\"x\"\"\":\n        send \"same\"\n");
        assertEquals(List.of("same"), runner.game.messages);
    }
}
