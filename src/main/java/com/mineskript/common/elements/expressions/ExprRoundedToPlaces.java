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

@Name("Rounded To Places")
@Description("Rounds a number to a given number of decimal places, halves rounding up. The place count is rounded and kept between 0 and 15. Useful for storing or comparing a tidy value, since text already shows at most 2 decimal places.")
@Examples({
        "on key press of \"c\":",
        "\tsend \"x: %player's x-coordinate rounded to 1 place%\""
})
@Since("1.0.0-alpha.2")
public final class ExprRoundedToPlaces implements Expression {
    private final Expression first;
    private final Expression second;

    private ExprRoundedToPlaces(Expression first, Expression second) {
        this.first = first;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprRoundedToPlaces::create, "%number% rounded to %number% (place|places)");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprRoundedToPlaces(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double a = (Double) first.evaluate(context);
        double b = (Double) second.evaluate(context);
        return MathHelper.places(a, b);
    }
}
