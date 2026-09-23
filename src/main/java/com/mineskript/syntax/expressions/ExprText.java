package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxFactory;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

public final class ExprText implements Expression {
    private enum Kind {
        UPPER,
        LOWER,
        LENGTH,
        RANGE,
        REPLACE,
        JOIN,
        FIRST,
        LAST
    }

    private final Kind kind;
    private final SkType type;
    private final Expression a;
    private final Expression b;
    private final Expression c;

    private ExprText(Kind kind, SkType type, Expression a, Expression b, Expression c) {
        this.kind = kind;
        this.type = type;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, one(Kind.UPPER), "(uppercase|upper case) %string%");
        registry.addExpression(SkType.TEXT, Tier.COMBINED, one(Kind.LOWER), "(lowercase|lower case) %string%");
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, one(Kind.LENGTH), "[the] length of %string%", "%string%'s length");
        registry.addExpression(SkType.TEXT, Tier.COMBINED, three(Kind.RANGE), "%string% from character %number% to %number%");
        registry.addExpression(SkType.TEXT, Tier.COMBINED, three(Kind.REPLACE), "%string% with %string% replaced with %string%");
        registry.addExpression(SkType.TEXT, Tier.COMBINED, two(Kind.JOIN), "%string% joined with %string%");
        registry.addExpression(SkType.TEXT, Tier.COMBINED, edge(Kind.FIRST), "first %number% (character|characters) of %string%");
        registry.addExpression(SkType.TEXT, Tier.COMBINED, edge(Kind.LAST), "last %number% (character|characters) of %string%");
    }

    private static SyntaxFactory<Expression> one(Kind kind) {
        SkType type = kind == Kind.LENGTH ? SkType.NUMBER : SkType.TEXT;
        return (match, scope) -> build(match, scope, kind, type, 1);
    }

    private static SyntaxFactory<Expression> two(Kind kind) {
        return (match, scope) -> build(match, scope, kind, SkType.TEXT, 2);
    }

    private static SyntaxFactory<Expression> three(Kind kind) {
        return (match, scope) -> build(match, scope, kind, SkType.TEXT, 3);
    }

    private static SyntaxFactory<Expression> edge(Kind kind) {
        return (match, scope) -> {
            if (match.slot(0).isList() || match.slot(1).isList()) {
                return Optional.empty();
            }
            return Optional.of(new ExprText(kind, SkType.TEXT, match.slot(1), match.slot(0), null));
        };
    }

    private static Optional<Expression> build(Match match, ParseScope scope, Kind kind, SkType type, int arity) {
        for (int i = 0; i < arity; i++) {
            if (match.slot(i).isList()) {
                return Optional.empty();
            }
        }
        Expression a = match.slot(0);
        Expression b = arity > 1 ? match.slot(1) : null;
        Expression c = arity > 2 ? match.slot(2) : null;
        return Optional.of(new ExprText(kind, type, a, b, c));
    }

    @Override
    public SkType type() {
        return type;
    }

    @Override
    public Object evaluate(Context context) {
        String text = Converters.toText(a.evaluate(context), context);
        return switch (kind) {
            case UPPER -> text.toUpperCase(Locale.ROOT);
            case LOWER -> text.toLowerCase(Locale.ROOT);
            case LENGTH -> (double) text.length();
            case JOIN -> text + Converters.toText(b.evaluate(context), context);
            case REPLACE -> text.replace(Converters.toText(b.evaluate(context), context),
                    Converters.toText(c.evaluate(context), context));
            case RANGE -> range(text, index(b, context), index(c, context));
            case FIRST -> range(text, 1, index(b, context));
            case LAST -> last(text, index(b, context));
        };
    }

    private static int index(Expression expression, Context context) {
        double value = (Double) expression.evaluate(context);
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.round(value);
    }

    private static String range(String text, int from, int to) {
        int start = Math.max(1, from) - 1;
        int end = Math.min(text.length(), to);
        if (start >= text.length() || end <= start) {
            return "";
        }
        return text.substring(start, end);
    }

    private static String last(String text, int count) {
        int take = Math.max(0, Math.min(text.length(), count));
        return text.substring(text.length() - take);
    }
}
