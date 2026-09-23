package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondGamemode implements Condition {
    private final Expression mode;
    private final boolean negate;

    private CondGamemode(Expression mode, boolean negate) {
        this.mode = mode;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, false), "[the] gamemode (is|are) %string%");
        registry.addCondition((match, scope) -> create(match, true),
                "[the] gamemode (isn't|is not|aren't|are not) %string%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression mode = match.slot(0);
        if (mode.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondGamemode(mode, negate));
    }

    @Override
    public boolean test(Context context) {
        Object value = mode.evaluate(context);
        boolean result = value instanceof String text && text.equalsIgnoreCase(context.world().gamemode());
        return negate != result;
    }
}
