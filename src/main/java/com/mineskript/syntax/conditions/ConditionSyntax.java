package com.mineskript.syntax.conditions;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class ConditionSyntax {
    private ConditionSyntax() {
    }

    public static void register(SyntaxRegistry registry) {
        CondPlayerState.register(registry);
        CondKeyHeld.register(registry);
        CondCompare.register(registry);
    }
}
