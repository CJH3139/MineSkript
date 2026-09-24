package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;

public record ConditionExpression(Condition condition) implements Expression {
    @Override
    public SkType type() {
        return SkType.BOOLEAN;
    }

    @Override
    public Object evaluate(Context context) {
        return condition.test(context);
    }
}
