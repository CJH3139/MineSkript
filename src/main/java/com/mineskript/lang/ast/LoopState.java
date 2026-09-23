package com.mineskript.lang.ast;

public final class LoopState {
    private Object value = None.NONE;
    private int iteration;

    public Object value() {
        return value;
    }

    public int iteration() {
        return iteration;
    }

    public void next(Object value) {
        this.value = value;
        iteration++;
    }
}
