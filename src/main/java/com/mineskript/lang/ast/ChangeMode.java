package com.mineskript.lang.ast;

/** The ways the set, add, remove, delete and reset effects can change an expression. */
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

    /** How the mode reads after "can only be" or "not", such as "added to" or "deleted". */
    public String phrase() {
        return phrase;
    }
}
