package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;

public record ConstantExpression(SkType type, Object value) implements Expression {
    @Override
    public Object evaluate(Context context) {
        return value;
    }
}
