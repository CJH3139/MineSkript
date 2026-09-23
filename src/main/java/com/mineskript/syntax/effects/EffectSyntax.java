package com.mineskript.syntax.effects;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class EffectSyntax {
    private EffectSyntax() {
    }

    public static void register(SyntaxRegistry registry) {
        EffWait.register(registry);
        EffExitLoop.register(registry);
        EffContinue.register(registry);
        EffStop.register(registry);
        EffMakeSay.register(registry);
        EffCommand.register(registry);
        EffAttackUse.register(registry);
        EffKey.register(registry);
        EffSelectSlot.register(registry);
        EffSwapHands.register(registry);
        EffDrop.register(registry);
        EffLookAt.register(registry);
        EffSetRotation.register(registry);
        EffHudText.register(registry);
        EffPlaySound.register(registry);
        EffCloseScreen.register(registry);
        EffOpenInventory.register(registry);
        EffClipboard.register(registry);
        EffScreenshot.register(registry);
        EffDisconnect.register(registry);
        EffChange.register(registry);
        EffSend.register(registry);
    }
}
