package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;

public final class WaitUntil implements Statement {
    public static final int TIMEOUT_TICKS = 600;

    private final int line;
    private final Condition condition;

    public WaitUntil(int line, Condition condition) {
        this.line = line;
        this.condition = condition;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        if (condition.test(context)) {
            return Flow.CONTINUE;
        }
        return new Flow.Park(condition, TIMEOUT_TICKS);
    }
}
