package com.mineskript.lang.ast;

public record Trigger(String file, int line, Event event, Block body) {
}
