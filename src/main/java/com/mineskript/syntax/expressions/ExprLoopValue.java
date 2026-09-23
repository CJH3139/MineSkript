package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.LoopKind;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprLoopValue implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.OBJECT, Tier.SIMPLE, (match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("loop-value is only available inside a loop");
            }
            if (scope.loop() == LoopKind.WHILE) {
                throw new SyntaxException("loop-value is not available inside while");
            }
            return Optional.of(new ExprLoopValue());
        }, "[the] [current] loop-value");
    }

    @Override
    public SkType type() {
        return SkType.OBJECT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.currentLoop().value();
    }
}
