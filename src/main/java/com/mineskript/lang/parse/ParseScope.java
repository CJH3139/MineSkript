package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Event;

public record ParseScope(String file, int line, Event event) {
}
