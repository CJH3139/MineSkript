package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

public final class CondEffect implements Condition {
    private final Expression effect;
    private final boolean negate;

    private CondEffect(Expression effect, boolean negate) {
        this.effect = effect;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, false),
                "%player% (has|have) [the] effect %string%");
        registry.addCondition((match, scope) -> create(match, true),
                "%player% (doesn't have|does not have|don't have|do not have) [the] effect %string%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        if (match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondEffect(match.slot(1), negate));
    }

    @Override
    public boolean test(Context context) {
        String name = Converters.toText(effect.evaluate(context), context);
        return negate != (context.world().effectLevel(name) > 0);
    }
}
