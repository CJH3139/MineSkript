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

@Name("Lowercase")
@Description("The text with every letter converted to lower case. Also written lower case. Useful for comparing text without caring about case.")
@Examples({
        "on chat:",
        "\tset {_m} to lowercase message",
        "\tif {_m} contains \"help\":",
        "\t\tsend \"someone needs help\""
})
@Since("1.0.0-alpha.2")
public final class ExprLowercase implements Expression {
    private final Expression text;

    private ExprLowercase(Expression text) {
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprLowercase::create, "(lowercase|lower case) %string%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprLowercase(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        return value.toLowerCase(Locale.ROOT);
    }
}
