package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;

public record ConvertedExpression(Expression inner, SkType type) implements Expression {
    @Override
    public Object evaluate(Context context) {
        return Converters.convert(inner.evaluate(context), type, context);
    }

    @Override
    public boolean isList() {
        return inner.isList();
    }
}
