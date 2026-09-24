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

@Name("Round")
@Description("Rounds a number to the nearest whole number. Halves round up, towards positive infinity, so round 2.5 is 3 and round -2.5 is -2.")
@Examples({
        "on key press of \"c\":",
        "	send \"block x: %round player's x-coordinate%\""
})
@Since("1.0.0-alpha.2")
public final class ExprRound implements Expression {
    private final Expression number;

    private ExprRound(Expression number) {
        this.number = number;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprRound::create, "round %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprRound(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double value = (Double) number.evaluate(context);
        return (double) Math.round(value);
    }
}
