package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import java.util.List;

public record ListExpression(List<Expression> items, SkType type, boolean disjunctive) implements Expression {
    @Override
    public Object evaluate(Context context) {
        return items.stream().map(item -> item.evaluate(context)).toList();
    }

    @Override
    public boolean isList() {
        return true;
    }
}
