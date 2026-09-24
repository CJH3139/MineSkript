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

@Name("Square Root")
@Description("The square root of a number. Also written sqrt of. The square root of a negative number is not a real number and prints as NaN.")
@Examples({
        "on key press of \"d\":",
        "\tset {_x} to player's x-coordinate",
        "\tset {_z} to player's z-coordinate",
        "\tset {_squared} to {_x} ^ 2 + {_z} ^ 2",
        "\tsend \"%sqrt of {_squared}% blocks from 0, 0\""
})
@Since("1.0.0-alpha.2")
public final class ExprSquareRoot implements Expression {
    private final Expression number;

    private ExprSquareRoot(Expression number) {
        this.number = number;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprSquareRoot::create, "[the] (square root|sqrt) of %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprSquareRoot(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double value = (Double) number.evaluate(context);
        return Math.sqrt(value);
    }
}
