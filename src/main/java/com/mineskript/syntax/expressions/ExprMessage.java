package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprMessage implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> {
            if (!(scope.event() instanceof Event.Chat)) {
                throw new SyntaxException("\"message\" is only available inside \"on chat\"");
            }
            return Optional.of(new ExprMessage());
        }, "[the] [chat] message");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValue("message");
    }
}
