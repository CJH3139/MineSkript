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

@Name("Ceiling")
@Description("Rounds a number up to the whole number at or above it, so ceiling 2.1 is 3 and ceiling -2.7 is -2. Also written ceil.")
@Examples({
        "on key press of \"h\":",
        "	set {_half} to health of player / 2",
        "	set {_hearts} to ceil {_half}",
        "	send \"%{_hearts}% hearts\""
})
@Since("1.0.0-alpha.2")
public final class ExprCeiling implements Expression {
    private final Expression number;

    private ExprCeiling(Expression number) {
        this.number = number;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprCeiling::create, "(ceiling|ceil) %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprCeiling(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double value = (Double) number.evaluate(context);
        return Math.ceil(value);
    }
}
