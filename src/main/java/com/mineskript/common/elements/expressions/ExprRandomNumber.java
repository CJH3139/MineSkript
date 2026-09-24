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

@Name("Random Number")
@Description("A random decimal number between two numbers. The order of the bounds does not matter. The lower bound can be returned but the upper bound never is (unless both are equal, in which case that value is returned).")
@Examples({
        "on key press of \"r\":",
        "	set {_chance} to random number between 0 and 1",
        "	if {_chance} is less than 0.25:",
        "		send \"lucky!\""
})
@Since("1.0.0-alpha.2")
public final class ExprRandomNumber implements Expression {
    private final Expression first;
    private final Expression second;

    private ExprRandomNumber(Expression first, Expression second) {
        this.first = first;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprRandomNumber::create, "[a] random number between %number% and %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprRandomNumber(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double a = (Double) first.evaluate(context);
        double b = (Double) second.evaluate(context);
        return MathHelper.random(a, b);
    }
}
