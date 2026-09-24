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

@Name("Replace Text")
@Description({
        "The first text with every occurrence of the second text replaced by the third. Matching is literal and case sensitive.",
        "An empty search text matches between every character, so it inserts the replacement between all characters."
})
@Examples({
        "on chat send:",
        "\tset {_new} to message with \"gg\" replaced with \"good game\"",
        "\tsend \"%{_new}%\""
})
@Since("1.0.0-alpha.2")
public final class ExprReplace implements Expression {
    private final Expression text;
    private final Expression second;
    private final Expression third;

    private ExprReplace(Expression text, Expression second, Expression third) {
        this.text = text;
        this.second = second;
        this.third = third;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprReplace::create, "%string% with %string% replaced with %string%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList() || match.slot(2).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprReplace(match.slot(0), match.slot(1), match.slot(2)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return value.replace(Converters.toText(second.evaluate(context), context),
                Converters.toText(third.evaluate(context), context));
    }
}
