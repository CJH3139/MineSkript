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
}
