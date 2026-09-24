package com.mineskript.common.elements.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

final class TextCheck implements Condition {
    enum Kind {
        CONTAINS,
        STARTS,
        ENDS,
        SAME
    }

    private final Kind kind;
    private final Expression left;
    private final Expression right;
    private final boolean negate;

    private TextCheck(Kind kind, Expression left, Expression right, boolean negate) {
        this.kind = kind;
        this.left = left;
        this.right = right;
        this.negate = negate;
    }

    static Optional<Condition> create(Match match, Kind kind, boolean negate) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new TextCheck(kind, match.slot(0), match.slot(1), negate));
    }

    @Override
    public boolean test(Context context) {
        String a = Converters.toText(left.evaluate(context), context);
        String b = Converters.toText(right.evaluate(context), context);
        boolean result = switch (kind) {
            case CONTAINS -> a.contains(b);
            case STARTS -> a.startsWith(b);
            case ENDS -> a.endsWith(b);
            case SAME -> a.toLowerCase(Locale.ROOT).equals(b.toLowerCase(Locale.ROOT));
        };
        return negate != result;
    }
}
