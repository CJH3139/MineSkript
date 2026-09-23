package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.VariableScope;

public record VariableExpression(VariableScope scope, String name) implements Expression {
    public static VariableExpression of(String token) {
        VariableScope.Parsed parsed = VariableScope.parse(token);
        if (parsed.name().isEmpty()) {
            throw new SyntaxException("empty variable name");
        }
        return new VariableExpression(parsed.scope(), parsed.name());
    }

    @Override
    public SkType type() {
        return SkType.OBJECT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.getVariable(scope, name);
    }
}
