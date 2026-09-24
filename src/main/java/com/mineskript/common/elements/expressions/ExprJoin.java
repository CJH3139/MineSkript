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
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Join Text")
@Description("The two texts joined together, with nothing added in between. Include any space you want in one of the texts.")
@Examples({
        "on key press of \"j\":",
        "	set {_a} to \"Mine\"",
        "	set {_b} to \"Skript\"",
        "	send \"%{_a} joined with {_b}%\""
})
@Since("1.0.0-alpha.2")
public final class ExprJoin implements Expression {
    private final Expression text;
    private final Expression second;

    private ExprJoin(Expression text, Expression second) {
        this.text = text;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprJoin::create, "%string% joined with %string%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprJoin(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return value + Converters.toText(second.evaluate(context), context);
    }
}
