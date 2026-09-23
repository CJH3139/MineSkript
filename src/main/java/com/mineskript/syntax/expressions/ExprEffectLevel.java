package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

public final class ExprEffectLevel implements Expression {
    private final Expression effect;

    private ExprEffectLevel(Expression effect) {
        this.effect = effect;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED,
                (match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new ExprEffectLevel(match.slot(0))),
                "[the] level of effect %string%",
                "[the] effect level of %string%");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().effectLevel(Converters.toText(effect.evaluate(context), context));
    }
}
