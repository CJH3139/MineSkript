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

@Name("Random Integer")
@Description("A random whole number between two numbers, with both ends included. The bounds are rounded to whole numbers first and may be given in either order.")
@Examples({
        "on key press of \"r\":",
        "	send \"you rolled %random integer between 1 and 6%\"",
        "",
        "on key press of \"r\":",
        "	select slot random integer between 0 and 8"
})
@Since("1.0.0-alpha.2")
public final class ExprRandomInteger implements Expression {
    private final Expression first;
    private final Expression second;

    private ExprRandomInteger(Expression first, Expression second) {
        this.first = first;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprRandomInteger::create, "[a] random integer between %number% and %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprRandomInteger(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double a = (Double) first.evaluate(context);
        double b = (Double) second.evaluate(context);
        return MathHelper.randomInteger(a, b);
    }
}
