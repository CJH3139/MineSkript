package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Maximum")
@Description("The larger of two numbers. Also written max of.")
@Examples({
        "on key press of \"m\":",
        "\tset {_arrows} to number of arrow in inventory",
        "\tset {_over} to {_arrows} - 64",
        "\tset {_extra} to max of {_over} and 0",
        "\tsend \"%{_extra}% arrows beyond one stack\""
})
@Since("1.0.0-alpha.2")
public final class ExprMaximum implements Expression {
    private final Expression first;
    private final Expression second;

    private ExprMaximum(Expression first, Expression second) {
        this.first = first;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprMaximum::create, "[the] (maximum|max) of %number% and %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprMaximum(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double a = (Double) first.evaluate(context);
        double b = (Double) second.evaluate(context);
        return Math.max(a, b);
    }
}
