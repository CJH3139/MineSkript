package com.mineskript.syntax.expressions;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class ExpressionSyntax {
    private ExpressionSyntax() {
    }

    public static void register(SyntaxRegistry registry) {
        ExprPlayer.register(registry);
        ExprMessage.register(registry);
        ExprEventValue.register(registry);
        ExprLoopValue.register(registry);
        ExprLoopIteration.register(registry);
        ExprHeldItem.register(registry);
        ExprPlayerState.register(registry);
        ExprWorld.register(registry);
        ExprTarget.register(registry);
        ExprNearest.register(registry);
        ExprItemProperty.register(registry);
        ExprEntityProperty.register(registry);
        ExprItemInSlot.register(registry);
        ExprInventoryCount.register(registry);
        ExprPlayerProperty.register(registry);
        ExprCoordinate.register(registry);
        ExprBlock.register(registry);
        ExprText.register(registry);
        ExprMath.register(registry);
        ExprClient.register(registry);
        ExprEffectLevel.register(registry);
        ExprBlockAt.register(registry);
        ExprTargetCoordinate.register(registry);
    }
}
