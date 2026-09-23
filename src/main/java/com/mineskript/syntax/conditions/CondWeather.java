package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondWeather implements Condition {
    private static final String KINDS = "(raining:raining|thundering:thundering)";

    private final boolean raining;
    private final boolean negate;

    private CondWeather(boolean raining, boolean negate) {
        this.raining = raining;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, false), "it is " + KINDS);
        registry.addCondition((match, scope) -> create(match, true), "it (isn't|is not) " + KINDS);
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        return Optional.of(new CondWeather(match.has("raining"), negate));
    }

    @Override
    public boolean test(Context context) {
        boolean value = raining ? context.world().isRaining() : context.world().isThundering();
        return negate != value;
    }
}
