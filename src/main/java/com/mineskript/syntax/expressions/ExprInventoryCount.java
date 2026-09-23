package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprInventoryCount implements Expression {
    private final Expression type;

    private ExprInventoryCount(Expression type) {
        this.type = type;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED,
                (match, scope) -> Optional.of(new ExprInventoryCount(match.slot(0))),
                "[the] number of %blocktype% in [the] inventory");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().countItem(((BlockType) type.evaluate(context)).id());
    }
}
