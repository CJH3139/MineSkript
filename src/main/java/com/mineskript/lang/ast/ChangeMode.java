package com.mineskript.lang.ast;

public enum ChangeMode {
    SET("set"),
    ADD("added to"),
    REMOVE("removed from"),
    REMOVE_ALL("used with remove all"),
    DELETE("deleted"),
    RESET("reset");

    private final String phrase;

    ChangeMode(String phrase) {
        this.phrase = phrase;
    }

    public String phrase() {
        return phrase;
    }
}
