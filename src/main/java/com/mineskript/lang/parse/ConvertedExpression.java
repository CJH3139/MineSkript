package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

public record ConvertedExpression(Expression inner, SkType type) implements Expression {
    @Override
    public Object evaluate(Context context) {
        return Converters.convert(inner.evaluate(context), type, context);
    }

    public boolean untyped() {
        return inner.type() == SkType.OBJECT;
    }

    @Override
    public boolean isList() {
        return inner.isList();
    }

    @Override
    public Optional<Changeable> changer() {
        return inner.changer();
    }
}
