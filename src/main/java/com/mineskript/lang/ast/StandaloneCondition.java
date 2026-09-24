package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;

public final class StandaloneCondition implements Statement {
    private final int line;
    private final Condition condition;

    public StandaloneCondition(int line, Condition condition) {
        this.line = line;
        this.condition = condition;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return condition.test(context) ? Flow.CONTINUE : Flow.EXIT_SECTION;
    }
}
