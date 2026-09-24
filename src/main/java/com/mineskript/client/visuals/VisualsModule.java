package com.mineskript.client.visuals;

import com.mineskript.client.visuals.elements.EffMoveClientEntity;
import com.mineskript.client.visuals.elements.EffRemoveBeam;
import com.mineskript.client.visuals.elements.EffRemoveClientEntity;
import com.mineskript.client.visuals.elements.EffSetHologramText;
import com.mineskript.client.visuals.elements.EffShowBeam;
import com.mineskript.client.visuals.elements.EffSpawnClientEntity;
import com.mineskript.client.visuals.elements.ExprLastSpawnedClientEntity;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

public final class VisualsModule implements SyntaxModule {
    @Override
    public String name() {
        return "visuals";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        ExprLastSpawnedClientEntity.register(registry);

        EffSpawnClientEntity.register(registry);
        EffMoveClientEntity.register(registry);
        EffSetHologramText.register(registry);
        EffRemoveClientEntity.register(registry);
        EffShowBeam.register(registry);
        EffRemoveBeam.register(registry);
    }
}
