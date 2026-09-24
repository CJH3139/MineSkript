package com.mineskript.client.hud;

import com.mineskript.client.hud.elements.EffActionBar;
import com.mineskript.client.hud.elements.EffClipboard;
import com.mineskript.client.hud.elements.EffCloseScreen;
import com.mineskript.client.hud.elements.EffHudText;
import com.mineskript.client.hud.elements.EffPlaySound;
import com.mineskript.client.hud.elements.EffScreenshot;
import com.mineskript.client.hud.elements.EffSendTitle;
import com.mineskript.client.hud.elements.ExprClipboard;
import com.mineskript.client.hud.elements.ExprFps;
import com.mineskript.client.hud.elements.ExprScreenTitle;
import com.mineskript.client.hud.elements.ExprScreenType;
import com.mineskript.client.hud.elements.HudEvents;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

public final class HudModule implements SyntaxModule {
    @Override
    public String name() {
        return "hud";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        HudEvents.register(registry);

        ExprScreenTitle.register(registry);
        ExprScreenType.register(registry);
        ExprFps.register(registry);
        ExprClipboard.register(registry);

        EffHudText.register(registry);
        EffSendTitle.register(registry);
        EffActionBar.register(registry);
        EffPlaySound.register(registry);
        EffCloseScreen.register(registry);
        EffClipboard.register(registry);
        EffScreenshot.register(registry);
    }
}
