package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.ScriptError;

public record ArithmeticExpression(char operator, Expression left, Expression right) implements Expression {
    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        Object a = left.evaluate(context);
        Object b = right.evaluate(context);
        if (a == None.NONE || b == None.NONE) {
            throw new ScriptError("variable is not set");
        }
        if (!(a instanceof Double x) || !(b instanceof Double y)) {
            throw new ScriptError("cannot apply " + operator + " to " + Converters.typeName(Converters.typeOf(a)) + " and " + Converters.typeName(Converters.typeOf(b)));
        }
        return switch (operator) {
            case '+' -> x + y;
            case '-' -> x - y;
            case '*' -> x * y;
            case '/' -> {
                if (y == 0.0) {
                    throw new ScriptError("division by zero");
                }
                yield x / y;
            }
            default -> Math.pow(x, y);
        };
    }
}
