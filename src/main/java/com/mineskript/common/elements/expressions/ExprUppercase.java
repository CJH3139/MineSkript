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
import java.util.Locale;
import java.util.Optional;

@Name("Uppercase")
@Description("The text with every letter converted to upper case. Also written upper case.")
@Examples({
        "on chat send:",
        "\tset {_loud} to uppercase message",
        "\tsend \"%{_loud}%\""
})
@Since("1.0.0-alpha.2")
public final class ExprUppercase implements Expression {
    private final Expression text;

    private ExprUppercase(Expression text) {
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprUppercase::create, "(uppercase|upper case) %string%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprUppercase(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return value.toUpperCase(Locale.ROOT);
    }
}
