package com.mineskript.lang.ast;

public enum SkType {
    TEXT,
    NUMBER,
    BOOLEAN,
    TIMESPAN,
    BLOCKTYPE,
    BLOCK,
    PLAYER,
    OBJECT;

    public boolean accepts(SkType other) {
        return this == OBJECT || this == other;
    }
}
