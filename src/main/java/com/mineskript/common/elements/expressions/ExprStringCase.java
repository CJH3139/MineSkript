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
import java.util.Locale;
import java.util.Optional;

@Name("Case Text")
@Description({
        "A copy of a text in upper case, lower case or proper case, like Skript's case text.",
        "uppercase X, upper case X, X in upper case and capitalized X turn every letter into a capital; lowercase X, lower case X and X in lower case make every letter small. As in Skript, capitalized means all capitals.",
        "proper case X (also title case, or X in proper case) gives every word a capital first letter and leaves the other letters alone; strict proper case also makes the other letters small, so \"hELLo world\" in strict proper case is \"Hello World\".",
        "Given a list of texts, it changes each of them."
})
@Examples({
        "on chat send:",
        "\tset {_loud} to uppercase message",
        "\tsend \"%{_loud}%\"",
        "",
        "on chat:",
        "\tset {_m} to message in lower case",
        "\tif {_m} contains \"help\":",
        "\t\tsend \"someone needs help\"",
        "",
        "on key press of \"n\":",
        "\tsend \"steve the builder\" in proper case"
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprStringCase implements Expression {
    private static final String PROPER = "(proper case|propercase|title case|titlecase)";

    private enum Mode {
        UPPER,
        LOWER,
        PROPER,
        STRICT_PROPER
    }

    private final Mode mode;
    private final Expression text;

    private ExprStringCase(Mode mode, Expression text) {
        this.mode = mode;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprStringCase::create,
                "(upper:uppercase|upper:upper case|lowercase|lower case) %strings%",
                "%strings% in (upper:uppercase|upper:upper case|lowercase|lower case)",
                "(capitalised|capitalized) %strings%",
                "%strings% in [lenient|strict:strict] " + PROPER,
                "[lenient|strict:strict] " + PROPER + " %strings%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        Mode mode = switch (match.patternIndex()) {
            case 0, 1 -> match.has("upper") ? Mode.UPPER : Mode.LOWER;
            case 2 -> Mode.UPPER;
            default -> match.has("strict") ? Mode.STRICT_PROPER : Mode.PROPER;
        };
        return Optional.of(new ExprStringCase(mode, match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public boolean isList() {
        return text.isList();
    }

    @Override
    public Object evaluate(Context context) {
        return TextHelper.map(text, context, value -> switch (mode) {
            case UPPER -> value.toUpperCase(Locale.ROOT);
            case LOWER -> value.toLowerCase(Locale.ROOT);
            case PROPER -> properCase(value);
            case STRICT_PROPER -> properCase(value.toLowerCase(Locale.ROOT));
        });
    }

    private static String properCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean startOfWord = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            result.append(startOfWord ? Character.toTitleCase(c) : c);
            startOfWord = Character.isWhitespace(c);
        }
        return result.toString();
    }
}
