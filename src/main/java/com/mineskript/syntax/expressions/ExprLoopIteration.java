package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprLoopIteration implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("loop-iteration is only available inside a loop");
            }
            return Optional.of(new ExprLoopIteration());
        }, "[the] (loop-iteration|loop-counter|loop iteration|loop counter)");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.currentLoop().iteration();
    }
}
