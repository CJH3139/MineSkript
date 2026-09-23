package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class TokenizerTest {
    @Test
    void splitsOnWhitespaceAndLowercasesUnquotedWords() {
        assertEquals(List.of(new Token("send", false), new Token("Hello World", true)), Tokenizer.tokenize("Send   \"Hello World\""));
    }

    @Test
    void keepsQuotedTextVerbatimIncludingSpacesAndHashes() {
        assertEquals(List.of(new Token("a # b, c", true)), Tokenizer.tokenize("\"a # b, c\""));
    }

    @Test
    void separatesCommasAndPossessives() {
        assertEquals(List.of(
                new Token("stone", false), new Token(",", false), new Token("dirt", false),
                new Token("or", false), new Token("player", false), new Token("'s", false), new Token("health", false)),
                Tokenizer.tokenize("stone, dirt or player's health"));
    }

    @Test
    void keepsSymbolsAttachedToWords() {
        assertEquals(List.of(new Token("x-coordinate", false), new Token(">=", false), new Token("-3.5", false)), Tokenizer.tokenize("X-Coordinate >= -3.5"));
    }

    @Test
    void rejectsUnterminatedQuotes() {
        assertThrows(TokenizeException.class, () -> Tokenizer.tokenize("send \"oops"));
    }

    @Test
    void emptyLineHasNoTokens() {
        assertEquals(List.of(), Tokenizer.tokenize("   "));
    }

    @Test
    void bracesMakeOneVariableToken() {
        assertEquals(List.of(new Token("set", false), new Token("{my counter}", false), new Token("to", false), new Token("5", false)),
                Tokenizer.tokenize("set {My   Counter} to 5"));
        assertEquals(List.of(new Token("{_x}", false), new Token("{-x}", false), new Token("{x}", false)),
                Tokenizer.tokenize("{_x} {-x} {x}"));
        assertEquals(List.of(new Token("{a}", false), new Token("+", false), new Token("{b}", false)),
                Tokenizer.tokenize("{a} + {b}"));
    }

    @Test
    void parenthesesAreTheirOwnTokens() {
        assertEquals(List.of(new Token("(", false), new Token("{a}", false), new Token("+", false), new Token("1", false),
                new Token(")", false), new Token("*", false), new Token("2", false)),
                Tokenizer.tokenize("({a} + 1) * 2"));
        assertEquals(List.of(new Token("-3", false), new Token("-", false), new Token("(", false), new Token("2", false), new Token(")", false)),
                Tokenizer.tokenize("-3 - (2)"));
    }

    @Test
    void bracesInsideQuotesStayText() {
        assertEquals(List.of(new Token("send", false), new Token("{not a var}", true)), Tokenizer.tokenize("send \"{not a var}\""));
    }

    @Test
    void rejectsUnterminatedVariable() {
        TokenizeException error = assertThrows(TokenizeException.class, () -> Tokenizer.tokenize("set {oops to 5"));
        assertEquals("unterminated variable", error.getMessage());
    }
}
