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

@Name("Minimum")
@Description("The smaller of two numbers. Also written min of.")
@Examples({
        "on key press of \"m\":",
        "	set {_have} to number of cobblestone in inventory",
        "	set {_stack} to min of {_have} and 64",
        "	send \"%{_stack}% cobblestone ready to place\""
})
@Since("1.0.0-alpha.2")
public final class ExprMinimum implements Expression {
    private final Expression first;
    private final Expression second;

    private ExprMinimum(Expression first, Expression second) {
        this.first = first;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprMinimum::create, "[the] (minimum|min) of %number% and %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprMinimum(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double a = (Double) first.evaluate(context);
        double b = (Double) second.evaluate(context);
        return Math.min(a, b);
    }
}
