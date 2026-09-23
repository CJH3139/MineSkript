package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprBlockAt implements Expression {
    private final Expression x;
    private final Expression y;
    private final Expression z;

    private ExprBlockAt(Expression x, Expression y, Expression z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.BLOCK, Tier.COMBINED, ExprBlockAt::create,
                "[the] block at %number%, %number%, %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList() || match.slot(2).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprBlockAt(match.slot(0), match.slot(1), match.slot(2)));
    }

    @Override
    public SkType type() {
        return SkType.BLOCK;
    }

    @Override
    public Object evaluate(Context context) {
        return new BlockValue(context.world().blockAt(
                (Double) x.evaluate(context), (Double) y.evaluate(context), (Double) z.evaluate(context)));
    }
}
