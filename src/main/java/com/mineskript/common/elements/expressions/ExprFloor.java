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

@Name("Floor")
@Description("Rounds a number down to the whole number at or below it, so floor 2.7 is 2 and floor -2.3 is -3. Flooring a player coordinate gives the coordinate of the block they are in.")
@Examples({
        "on key press of \"c\":",
        "\tsend \"block %floor player's x-coordinate%, %floor player's y-coordinate%, %floor player's z-coordinate%\""
})
@Since("1.0.0-alpha.2")
public final class ExprFloor implements Expression {
    private final Expression number;

    private ExprFloor(Expression number) {
        this.number = number;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprFloor::create, "floor %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprFloor(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double value = (Double) number.evaluate(context);
        return Math.floor(value);
    }
}
