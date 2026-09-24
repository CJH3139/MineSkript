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

@Name("First Characters")
@Description("The first N characters of a text. A count larger than the text gives the whole text, and 0 or less gives empty text. The count is rounded.")
@Examples({
        "on chat:",
        "\tset {_start} to first 5 characters of message",
        "\tsend \"starts with: %{_start}%\""
})
@Since("1.0.0-alpha.2")
public final class ExprFirstCharacters implements Expression {
    private final Expression count;
    private final Expression text;

    private ExprFirstCharacters(Expression count, Expression text) {
        this.count = count;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprFirstCharacters::create, "first %number% (character|characters) of %string%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprFirstCharacters(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return TextHelper.range(value, 1, TextHelper.index(count, context));
    }
}
