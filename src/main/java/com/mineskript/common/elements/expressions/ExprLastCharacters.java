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

@Name("Last Characters")
@Description("The last N characters of a text. A count larger than the text gives the whole text, and 0 or less gives empty text. The count is rounded.")
@Examples({
        "on key press of \"t\":",
        "\tset {_id} to id of held item",
        "\tsend \"ends in %last 3 characters of {_id}%\""
})
@Since("1.0.0-alpha.2")
public final class ExprLastCharacters implements Expression {
    private final Expression count;
    private final Expression text;

    private ExprLastCharacters(Expression count, Expression text) {
        this.count = count;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprLastCharacters::create, "last %number% (character|characters) of %string%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprLastCharacters(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return TextHelper.last(value, TextHelper.index(count, context));
    }
}
