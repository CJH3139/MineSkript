package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.LoopKind;

public record ParseScope(String file, int line, Event event, int loopDepth, LoopKind loop) {
    public ParseScope(String file, int line, Event event) {
        this(file, line, event, 0, null);
    }

    public boolean canWait() {
        return event == null || !event.context().instant();
    }

    public boolean inLoop() {
        return loopDepth > 0;
    }
}
