package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class ExprMath implements Expression {
    private enum Kind {
        ROUND,
        FLOOR,
        CEILING,
        ABSOLUTE,
        PLACES,
        MINIMUM,
        MAXIMUM,
        RANDOM,
        RANDOM_INTEGER,
        SQUARE_ROOT
    }

    private final Kind kind;
    private final Expression a;
    private final Expression b;

    private ExprMath(Kind kind, Expression a, Expression b) {
        this.kind = kind;
        this.a = a;
        this.b = b;
    }

    public static void register(SyntaxRegistry registry) {
        unary(registry, Kind.ROUND, "round %number%");
        unary(registry, Kind.FLOOR, "floor %number%");
        unary(registry, Kind.CEILING, "(ceiling|ceil) %number%");
        unary(registry, Kind.ABSOLUTE, "[the] (absolute value|abs) of %number%");
        unary(registry, Kind.SQUARE_ROOT, "[the] (square root|sqrt) of %number%");
        binary(registry, Kind.PLACES, "%number% rounded to %number% (place|places)");
        binary(registry, Kind.MINIMUM, "[the] (minimum|min) of %number% and %number%");
        binary(registry, Kind.MAXIMUM, "[the] (maximum|max) of %number% and %number%");
        binary(registry, Kind.RANDOM, "[a] random number between %number% and %number%");
        binary(registry, Kind.RANDOM_INTEGER, "[a] random integer between %number% and %number%");
    }

    private static void unary(SyntaxRegistry registry, Kind kind, String pattern) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED,
                (match, scope) -> build(match, kind, 1), pattern);
    }

    private static void binary(SyntaxRegistry registry, Kind kind, String pattern) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED,
                (match, scope) -> build(match, kind, 2), pattern);
    }

    private static Optional<Expression> build(Match match, Kind kind, int arity) {
        for (int i = 0; i < arity; i++) {
            if (match.slot(i).isList()) {
                return Optional.empty();
            }
        }
        return Optional.of(new ExprMath(kind, match.slot(0), arity > 1 ? match.slot(1) : null));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        double first = (Double) a.evaluate(context);
        return switch (kind) {
            case ROUND -> (double) Math.round(first);
            case FLOOR -> Math.floor(first);
            case CEILING -> Math.ceil(first);
            case ABSOLUTE -> Math.abs(first);
            case SQUARE_ROOT -> Math.sqrt(first);
            case PLACES -> places(first, (Double) b.evaluate(context));
            case MINIMUM -> Math.min(first, (Double) b.evaluate(context));
            case MAXIMUM -> Math.max(first, (Double) b.evaluate(context));
            case RANDOM -> random(first, (Double) b.evaluate(context));
            case RANDOM_INTEGER -> randomInteger(first, (Double) b.evaluate(context));
        };
    }

    private static double places(double value, double digits) {
        int places = Math.max(0, Math.min(15, (int) Math.round(digits)));
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    private static double random(double from, double to) {
        double low = Math.min(from, to);
        double high = Math.max(from, to);
        if (low == high) {
            return low;
        }
        return ThreadLocalRandom.current().nextDouble(low, high);
    }

    private static double randomInteger(double from, double to) {
        long first = Math.round(from);
        long second = Math.round(to);
        long low = Math.min(first, second);
        long high = Math.max(first, second);
        if (low == high) {
            return low;
        }
        if (high == Long.MAX_VALUE) {
            if (low == Long.MIN_VALUE) {
                return ThreadLocalRandom.current().nextLong();
            }
            return ThreadLocalRandom.current().nextLong(low - 1, high) + 1;
        }
        return ThreadLocalRandom.current().nextLong(low, high + 1);
    }
}
