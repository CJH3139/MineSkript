package com.mineskript.lang.ast;

public record EventValue(String name, SkType type, String syntax, String description) {
    public static EventValue of(String name, SkType type, String description) {
        return new EventValue(name, type, "event-" + name, description);
    }

    public boolean generic() {
        return syntax.equals("event-" + name);
    }
}
