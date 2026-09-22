package com.mineskript.syntax;

import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.syntax.conditions.ConditionSyntax;
import com.mineskript.syntax.effects.EffectSyntax;
import com.mineskript.syntax.events.EventSyntax;
import com.mineskript.syntax.expressions.ExpressionSyntax;

public final class DefaultSyntax {
    private DefaultSyntax() {
    }

    public static void registerAll(SyntaxRegistry registry) {
        EventSyntax.register(registry);
        ExpressionSyntax.register(registry);
        ConditionSyntax.register(registry);
        EffectSyntax.register(registry);
    }

    public static SyntaxRegistry registry() {
        SyntaxRegistry registry = new SyntaxRegistry();
        registerAll(registry);
        return registry;
    }
}
