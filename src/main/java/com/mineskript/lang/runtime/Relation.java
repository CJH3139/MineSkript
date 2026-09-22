package com.mineskript.lang.runtime;

public enum Relation {
    EQUAL,
    NOT_EQUAL,
    GREATER,
    GREATER_OR_EQUAL,
    LESS,
    LESS_OR_EQUAL;

    public boolean ordered() {
        return this != EQUAL && this != NOT_EQUAL;
    }

    public boolean holds(int sign) {
        return switch (this) {
            case EQUAL -> sign == 0;
            case NOT_EQUAL -> sign != 0;
            case GREATER -> sign > 0;
            case GREATER_OR_EQUAL -> sign >= 0;
            case LESS -> sign < 0;
            case LESS_OR_EQUAL -> sign <= 0;
        };
    }
}
