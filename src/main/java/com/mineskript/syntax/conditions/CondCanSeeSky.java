package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondCanSeeSky implements Condition {
    private final boolean negate;

    private CondCanSeeSky(boolean negate) {
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> Optional.of(new CondCanSeeSky(false)),
                "%player% (can see|sees) [the] sky");
        registry.addCondition((match, scope) -> Optional.of(new CondCanSeeSky(true)),
                "%player% (can't see|cannot see|can not see|doesn't see|does not see) [the] sky");
    }

    @Override
    public boolean test(Context context) {
        return negate != context.world().canSeeSky();
    }
}
