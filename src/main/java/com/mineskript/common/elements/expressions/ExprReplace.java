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
import com.mineskript.lang.runtime.TextMatching;
import java.util.Optional;

@Name("Replace Text")
@Description({
        "The first text with every occurrence of the second text replaced by the third. Matching is literal and ignores capitals, like the Replace effect and Skript's default; add with case sensitivity to only replace text whose capitals match too.",
        "An empty search text matches between every character, so it inserts the replacement between all characters. To change a variable or the message itself, use the Replace effect: replace \"a\" with \"b\" in {_text}."
})
@Examples({
        "on chat send:",
        "\tset {_new} to message with \"gg\" replaced with \"good game\"",
        "\tsend \"%{_new}%\"",
        "",
        "on chat send:",
        "\tsend message with \"GG\" replaced with \"good game\" with case sensitivity"
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprReplace implements Expression {
    private final Expression text;
    private final Expression second;
    private final Expression third;
    private final boolean caseSensitive;

    private ExprReplace(Expression text, Expression second, Expression third, boolean caseSensitive) {
        this.text = text;
        this.second = second;
        this.third = third;
        this.caseSensitive = caseSensitive;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprReplace::create,
                "%string% with %string% replaced with %string% [case:with case sensitivity]");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList() || match.slot(2).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprReplace(match.slot(0), match.slot(1), match.slot(2), match.has("case")));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return TextMatching.replace(value, Converters.toText(second.evaluate(context), context),
                Converters.toText(third.evaluate(context), context), caseSensitive, false);
    }
}
