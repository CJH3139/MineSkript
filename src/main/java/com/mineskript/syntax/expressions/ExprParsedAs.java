package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

public final class ExprParsedAs implements Expression {
    private enum Kind {
        NUMBER,
        INTEGER,
        BOOLEAN
    }

    private final Expression text;
    private final Kind kind;

    private ExprParsedAs(Expression text, Kind kind) {
        this.text = text;
        this.kind = kind;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, (match, scope) -> create(match, scope, Kind.NUMBER), "%string% parsed as [a] number");
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, (match, scope) -> create(match, scope, Kind.INTEGER), "%string% parsed as [an] integer");
        registry.addExpression(SkType.BOOLEAN, Tier.COMBINED, (match, scope) -> create(match, scope, Kind.BOOLEAN), "%string% parsed as [a] boolean");
    }

    private static Optional<Expression> create(Match match, ParseScope scope, Kind kind) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprParsedAs(match.slot(0), kind));
    }

    @Override
    public SkType type() {
        return kind == Kind.BOOLEAN ? SkType.BOOLEAN : SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context).strip();
        return switch (kind) {
            case NUMBER -> number(value);
            case INTEGER -> {
                Object number = number(value);
                yield number instanceof Double parsed && parsed == Math.rint(parsed) ? parsed : None.NONE;
            }
            case BOOLEAN -> switch (value.toLowerCase(Locale.ROOT)) {
                case "true", "yes", "on" -> Boolean.TRUE;
                case "false", "no", "off" -> Boolean.FALSE;
                default -> None.NONE;
            };
        };
    }

    private static Object number(String value) {
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : None.NONE;
        } catch (NumberFormatException error) {
            return None.NONE;
        }
    }
}
