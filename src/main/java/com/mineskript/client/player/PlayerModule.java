package com.mineskript.client.player;

import com.mineskript.client.player.elements.CondCanSeeSky;
import com.mineskript.client.player.elements.CondGamemode;
import com.mineskript.client.player.elements.CondHasEffect;
import com.mineskript.client.player.elements.CondPlayerFlag;
import com.mineskript.client.player.elements.CondPlayerState;
import com.mineskript.client.player.elements.CondWithinBlocks;
import com.mineskript.client.player.elements.CondWithinBlocksOfPoint;
import com.mineskript.client.player.elements.ExprAir;
import com.mineskript.client.player.elements.ExprArmor;
import com.mineskript.client.player.elements.ExprCoordinate;
import com.mineskript.client.player.elements.ExprEffectLevel;
import com.mineskript.client.player.elements.ExprFallDistance;
import com.mineskript.client.player.elements.ExprGamemode;
import com.mineskript.client.player.elements.ExprHealth;
import com.mineskript.client.player.elements.ExprHunger;
import com.mineskript.client.player.elements.ExprMaxAir;
import com.mineskript.client.player.elements.ExprMaxHealth;
import com.mineskript.client.player.elements.ExprPlayer;
import com.mineskript.client.player.elements.ExprPlayerName;
import com.mineskript.client.player.elements.ExprSaturation;
import com.mineskript.client.player.elements.ExprSpeed;
import com.mineskript.client.player.elements.ExprTotalExperience;
import com.mineskript.client.player.elements.ExprXpLevel;
import com.mineskript.client.player.elements.ExprXpProgress;
import com.mineskript.client.player.elements.PlayerEvents;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

public final class PlayerModule implements SyntaxModule {
    @Override
    public String name() {
        return "player";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        PlayerEvents.register(registry);

        ExprPlayer.register(registry);
        ExprGamemode.register(registry);
        ExprXpLevel.register(registry);
        ExprXpProgress.register(registry);
        ExprAir.register(registry);
        ExprMaxAir.register(registry);
        ExprArmor.register(registry);
        ExprSpeed.register(registry);
        ExprFallDistance.register(registry);
        ExprHealth.register(registry);
        ExprMaxHealth.register(registry);
        ExprHunger.register(registry);
        ExprPlayerName.register(registry);
        ExprCoordinate.register(registry);
        ExprSaturation.register(registry);
        ExprTotalExperience.register(registry);
        ExprEffectLevel.register(registry);

        CondPlayerState.register(registry);
        CondPlayerFlag.register(registry);
        CondCanSeeSky.register(registry);
        CondHasEffect.register(registry);
        CondGamemode.register(registry);
        CondWithinBlocks.register(registry);
        CondWithinBlocksOfPoint.register(registry);
    }
}
