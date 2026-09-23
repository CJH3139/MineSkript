package com.mineskript.lang.runtime;

public enum VariableScope {
    GLOBAL,
    RAM,
    LOCAL;

    public record Parsed(VariableScope scope, String name) {
    }

    public static Parsed parse(String token) {
        String inner = token.substring(1, token.length() - 1).trim();
        if (inner.startsWith("_")) {
            return new Parsed(LOCAL, inner.substring(1).trim());
        }
        if (inner.startsWith("-")) {
            return new Parsed(RAM, inner.substring(1).trim());
        }
        return new Parsed(GLOBAL, inner);
    }
}
