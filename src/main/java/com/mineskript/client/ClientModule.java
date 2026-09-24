package com.mineskript.client;

import com.mineskript.client.chat.ChatModule;
import com.mineskript.client.entity.EntityModule;
import com.mineskript.client.hud.HudModule;
import com.mineskript.client.inventory.InventoryModule;
import com.mineskript.client.movement.MovementModule;
import com.mineskript.client.player.PlayerModule;
import com.mineskript.client.server.ServerModule;
import com.mineskript.client.visuals.VisualsModule;
import com.mineskript.client.world.WorldModule;
import com.mineskript.lang.module.HierarchicalModule;

public final class ClientModule extends HierarchicalModule {
    public ClientModule() {
        super(new MovementModule(), new InventoryModule(), new ChatModule(), new HudModule(), new WorldModule(),
                new EntityModule(), new PlayerModule(), new ServerModule(), new VisualsModule());
    }

    @Override
    public String name() {
        return "client";
    }
}
