package com.mineskript.lang.parse;

public record Token(String text, boolean quoted) {
    public boolean is(String word) {
        return !quoted && text.equals(word);
    }
}
