package com.mineskript.script;

public record MessageLine(MessageLine.Kind kind, String text) {
    public enum Kind {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        DETAIL
    }
}
