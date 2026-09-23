package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;

public final class TryStatement implements Statement {
    private final int line;
    private final Block body;
    private final Block handler;

    public TryStatement(int line, Block body, Block handler) {
        this.line = line;
        this.body = body;
        this.handler = handler;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return new Flow.EnterTry(body, handler);
    }
}
