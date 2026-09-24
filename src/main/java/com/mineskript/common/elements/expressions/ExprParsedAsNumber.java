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

@Name("Parsed As Number")
@Description({
        "Turns text into a number, such as 42 or -3.5 (surrounding spaces are ignored). Returns none when the text is not a number, so check it with is set before doing arithmetic.",
        "It accepts the same forms as Java number parsing, so scientific notation such as 1e3 is also read."
})
@Examples({
        "on chat send:",
        "\tset {_n} to message parsed as number",
        "\tif {_n} is set:",
        "\t\tsend \"double that is %{_n} * 2%\"",
        "",
        "on key press of \"n\":",
        "\tset {_t} to \"41\"",
        "\tsend \"%({_t} parsed as number) + 1%\""
})
@Since("1.0.0-alpha.5")
public final class ExprParsedAsNumber implements Expression {
    private final Expression text;

    private ExprParsedAsNumber(Expression text) {
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprParsedAsNumber::create, "%string% parsed as [a] number");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprParsedAsNumber(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context).strip();
        return ParsedNumbers.number(value);
    }
}
