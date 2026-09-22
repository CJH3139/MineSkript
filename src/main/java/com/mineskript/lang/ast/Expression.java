package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;

public interface Expression {
    SkType type();

    Object evaluate(Context context);

    default boolean isList() {
        return false;
    }
}
