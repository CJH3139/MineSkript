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

@Name("Text Range")
@Description("Part of a text between two character positions, counting from 1, with both ends included: abcdef from character 2 to 4 is bcd. Positions are rounded and clipped to the text, and a range that falls outside it gives empty text.")
@Examples({
        "on key press of \"t\":",
        "\tset {_t} to \"minecraft\"",
        "\tsend \"%{_t} from character 5 to 9%\""
})
@Since("1.0.0-alpha.2")
public final class ExprSubstring implements Expression {
    private final Expression text;
    private final Expression second;
    private final Expression third;

    private ExprSubstring(Expression text, Expression second, Expression third) {
        this.text = text;
        this.second = second;
        this.third = third;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprSubstring::create, "%string% from character %number% to %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList() || match.slot(2).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprSubstring(match.slot(0), match.slot(1), match.slot(2)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return TextHelper.range(value, TextHelper.index(second, context), TextHelper.index(third, context));
    }
}
