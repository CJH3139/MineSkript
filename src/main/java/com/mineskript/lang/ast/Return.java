package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;

public final class Return implements Statement {
    private final int line;
    private final Expression value;
    private final SkType type;

    public Return(int line, Expression value, SkType type) {
        this.line = line;
        this.value = value;
        this.type = type;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        if (value == null) {
            return new Flow.Return(None.NONE);
        }
        Object result = value.evaluate(context);
        return new Flow.Return(result == None.NONE ? None.NONE : Converters.convert(result, type, context));
    }
}
