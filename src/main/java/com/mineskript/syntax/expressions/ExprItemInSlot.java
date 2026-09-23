package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprItemInSlot implements Expression {
    private final Expression slot;

    private ExprItemInSlot(Expression slot) {
        this.slot = slot;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ITEM, Tier.COMBINED,
                (match, scope) -> Optional.of(new ExprItemInSlot(match.slot(0))),
                "[the] item in slot %number%");
    }

    @Override
    public SkType type() {
        return SkType.ITEM;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().itemInSlot((int) Math.round((Double) slot.evaluate(context)));
    }
}
