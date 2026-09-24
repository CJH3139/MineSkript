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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Name("Split Text")
@Description({
        "Splits text into a list of pieces at every occurrence of a separator. Also written split X at Y, and by or on work instead of at. The separator is matched literally and case sensitively; empty pieces are kept, so a,,b gives three pieces. An empty separator splits into single characters, and empty text gives an empty list.",
        "The result is a list, so use it with loop, contains or is in, or keep its pieces in a list variable with set {_pieces::*} to X split at Y, which stores them under the indices 1, 2, 3 and so on."
})
@Examples({
        "on key press of \"l\":",
        "\tloop \"a,b,c\" split at \",\":",
        "\t\tsend loop-value",
        "",
        "on chat:",
        "\tloop split message by \" \":",
        "\t\tif loop-value is \"hello\":",
        "\t\t\tsend \"someone said hello\""
})
@Since("1.0.0-alpha.4")
public final class ExprSplit implements Expression {
    private final Expression text;
    private final Expression delimiter;

    private ExprSplit(Expression text, Expression delimiter) {
        this.text = text;
        this.delimiter = delimiter;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprSplit::create,
                "%string% split (at|by|on) %string%",
                "split %string% (at|by|on) %string%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprSplit(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public Object evaluate(Context context) {
        String whole = Converters.toText(text.evaluate(context), context);
        String separator = Converters.toText(delimiter.evaluate(context), context);
        if (whole.isEmpty()) {
            return List.of();
        }
        if (separator.isEmpty()) {
            return whole.codePoints().mapToObj(Character::toString).toList();
        }
        return Arrays.stream(whole.split(Pattern.quote(separator), -1)).toList();
    }
}
