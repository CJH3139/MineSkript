package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;

public record TernaryExpression(Expression whenTrue, Condition condition, Expression whenFalse) implements Expression {
    @Override
    public SkType type() {
        return whenTrue.type() == whenFalse.type() ? whenTrue.type() : SkType.OBJECT;
    }

    @Override
    public Object evaluate(Context context) {
        return condition.test(context) ? whenTrue.evaluate(context) : whenFalse.evaluate(context);
    }

    @Override
    public boolean isList() {
        return whenTrue.isList() || whenFalse.isList();
    }
}
