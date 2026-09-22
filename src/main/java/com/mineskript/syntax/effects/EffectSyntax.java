package com.mineskript.syntax.effects;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class EffectSyntax {
    private EffectSyntax() {
    }

    public static void register(SyntaxRegistry registry) {
        EffWait.register(registry);
        EffStop.register(registry);
        EffMakeSay.register(registry);
        EffCommand.register(registry);
        EffAttackUse.register(registry);
        EffKey.register(registry);
        EffSend.register(registry);
    }
}
