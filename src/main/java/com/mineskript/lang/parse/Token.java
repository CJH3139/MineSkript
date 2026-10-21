package com.mineskript.lang.parse;

import java.util.Objects;

/**
 * One word, symbol or quoted text of a line. {@code text} is lower case (quoted text keeps its case) and is what
 * patterns match; {@code original} is the word exactly as it was typed, for syntax that needs its capitals. Two tokens
 * are equal when their text and quoting are, whatever their original spelling; {@link #toString()} includes the
 * original spelling when it differs, so parse caches keyed on it keep differently spelled words apart.
 */
public record Token(String text, boolean quoted, String original) {
    public Token(String text, boolean quoted) {
        this(text, quoted, text);
    }

    public boolean is(String word) {
        return !quoted && text.equals(word);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Token token && token.quoted == quoted && token.text.equals(text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, quoted);
    }

    @Override
    public String toString() {
        return "Token[text=" + text + ", quoted=" + quoted + (original.equals(text) ? "" : ", original=" + original) + "]";
    }
}
