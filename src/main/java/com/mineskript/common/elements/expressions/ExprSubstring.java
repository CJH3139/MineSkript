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
import java.util.Optional;

@Name("Substring")
@Description({
        "Part of a text, like Skript's substring: the first or last characters, or the characters between two positions.",
        "part of X between 2 and 4 (also subtext of, substring of, from ... to, and with index or character before the numbers) is the text from the second to the fourth character, counting from 1, with both ends included, so the part of \"abcdef\" from 2 to 4 is bcd. X from character 2 to 4 is the older spelling and still works.",
        "first 5 characters of X and the 5 first characters of X give the start of the text, last 3 characters of X its end, and first character of X or last character of X a single character.",
        "Positions and counts are rounded and clipped to the text: a count larger than the text gives the whole text, and 0 or less, or a range outside the text, gives empty text. Given a list of texts, it gives the part of each."
})
@Examples({
        "on chat:",
        "	set {_start} to first 5 characters of message",
        "	send \"starts with: %{_start}%\"",
        "",
        "on key press of \"t\":",
        "	set {_id} to id of held item",
        "	send \"ends in %last 3 characters of {_id}%\"",
        "",
        "on key press of \"t\":",
        "	set {_t} to \"minecraft\"",
        "	send \"%subtext of {_t} from characters 5 to 9%\"",
        "	send \"%{_t} from character 1 to 4%\"",
        "	send \"%the first character of {_t}%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprSubstring implements Expression {
    private enum Kind {
        RANGE,
        FIRST,
        LAST
    }

    private final Kind kind;
    private final Expression text;
    private final Expression first;
    private final Expression second;

    private ExprSubstring(Kind kind, Expression text, Expression first, Expression second) {
        this.kind = kind;
        this.text = text;
        this.first = first;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprSubstring::create,
                "[the] (part|subtext|sub text|substring|sub string) of %strings% (between|from) "
                        + "(index|indices|character|characters|) %number% (and|to) (index|character|) %number%",
                "[the] (first:first|last) [%-number%] (character|characters) of %strings%",
                "[the] %number% (first:first|last) characters of %strings%",
                "%string% from character %number% to %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        return switch (match.patternIndex()) {
            case 0, 3 -> range(match.slot(0), match.slot(1), match.slot(2));
            default -> ends(match.has("first") ? Kind.FIRST : Kind.LAST, match.slot(1), match.slot(0));
        };
    }

    private static Optional<Expression> range(Expression text, Expression from, Expression to) {
        if (from.isList() || to.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprSubstring(Kind.RANGE, text, from, to));
    }

    private static Optional<Expression> ends(Kind kind, Expression text, Expression count) {
        if (count != null && count.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprSubstring(kind, text, count, null));
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
        return switch (kind) {
            case RANGE -> {
                int from = TextHelper.index(first, context);
                int to = TextHelper.index(second, context);
                yield TextHelper.map(text, context, value -> TextHelper.range(value, from, to));
            }
            case FIRST -> {
                int count = count(context);
                yield TextHelper.map(text, context, value -> TextHelper.range(value, 1, count));
            }
            case LAST -> {
                int count = count(context);
                yield TextHelper.map(text, context, value -> TextHelper.last(value, count));
            }
        };
    }

    private int count(Context context) {
        return first == null ? 1 : TextHelper.index(first, context);
    }
}
