package com.mineskript.syntax.conditions;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class ConditionSyntax {
    private ConditionSyntax() {
    }

    public static void register(SyntaxRegistry registry) {
        CondIsSet.register(registry);
        CondPlayerState.register(registry);
        CondPlayerFlag.register(registry);
        CondCanSeeSky.register(registry);
        CondEffect.register(registry);
        CondHasItem.register(registry);
        CondInventory.register(registry);
        CondWeather.register(registry);
        CondGamemode.register(registry);
        CondWithin.register(registry);
        CondKeyHeld.register(registry);
        CondMembership.register(registry);
        CondText.register(registry);
        CondCompare.register(registry);
    }
}
