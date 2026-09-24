package com.mineskript.lang.ast;

public final class LoopState {
    private Object value = None.NONE;
    private String index;
    private int iteration;

    public Object value() {
        return value;
    }

    public int iteration() {
        return iteration;
    }

    /** The index of the current list variable entry, or the pass number as text for any other loop. */
    public String index() {
        return index != null ? index : String.valueOf(iteration);
    }

    public void next(Object value) {
        next(value, null);
    }

    public void next(Object value, String index) {
        this.value = value;
        this.index = index;
        iteration++;
    }
}
