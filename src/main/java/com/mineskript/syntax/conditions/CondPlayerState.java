package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondPlayerState implements Condition {
    private static final String STATES = "(sneaking:sneaking|ground:on [the] ground|sprinting:sprinting)";

    private final String state;
    private final boolean negate;

    private CondPlayerState(String state, boolean negate) {
        this.state = state;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, scope, false), "%player% (is|are) " + STATES);
        registry.addCondition((match, scope) -> create(match, scope, true), "%player% (isn't|is not|aren't|are not) " + STATES);
    }

    private static Optional<Condition> create(Match match, ParseScope scope, boolean negate) {
        String state = match.has("sneaking") ? "sneaking" : match.has("ground") ? "ground" : "sprinting";
        return Optional.of(new CondPlayerState(state, negate));
    }

    @Override
    public boolean test(Context context) {
        boolean value = switch (state) {
            case "sneaking" -> context.world().isSneaking();
            case "ground" -> context.world().isOnGround();
            default -> context.world().isSprinting();
        };
        return negate != value;
    }
}
