package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

public final class CondText implements Condition {
    private enum Kind {
        CONTAINS,
        STARTS,
        ENDS,
        SAME
    }

    private final Kind kind;
    private final Expression left;
    private final Expression right;
    private final boolean negate;

    private CondText(Kind kind, Expression left, Expression right, boolean negate) {
        this.kind = kind;
        this.left = left;
        this.right = right;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        add(registry, Kind.CONTAINS, false, "%string% (contains|contain) %string%");
        add(registry, Kind.CONTAINS, true, "%string% (doesn't contain|does not contain|don't contain|do not contain) %string%");
        add(registry, Kind.STARTS, false, "%string% (starts with|start with) %string%");
        add(registry, Kind.STARTS, true, "%string% (doesn't start with|does not start with|don't start with|do not start with) %string%");
        add(registry, Kind.ENDS, false, "%string% (ends with|end with) %string%");
        add(registry, Kind.ENDS, true, "%string% (doesn't end with|does not end with|don't end with|do not end with) %string%");
        add(registry, Kind.SAME, false, "%string% (is|are) %string% ignoring case");
        add(registry, Kind.SAME, true, "%string% (isn't|is not|aren't|are not) %string% ignoring case");
    }

    private static void add(SyntaxRegistry registry, Kind kind, boolean negate, String pattern) {
        registry.addCondition((match, scope) -> create(match, kind, negate), pattern);
    }

    private static Optional<Condition> create(Match match, Kind kind, boolean negate) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondText(kind, match.slot(0), match.slot(1), negate));
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
