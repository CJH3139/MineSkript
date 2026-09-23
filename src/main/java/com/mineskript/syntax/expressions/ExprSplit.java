package com.mineskript.syntax.expressions;

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
