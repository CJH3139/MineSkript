package com.mineskript.lang;

public record ParseError(String file, int line, String message) {
    @Override
    public String toString() {
        return line > 0 ? file + ":" + line + ": " + message : file + ": " + message;
    }
}
