package com.mineskript.client.entity;

import com.mineskript.client.entity.elements.EntityEvents;
import com.mineskript.client.entity.elements.ExprEntityCoordinate;
import com.mineskript.client.entity.elements.ExprEntityDistance;
import com.mineskript.client.entity.elements.ExprEntityId;
import com.mineskript.client.entity.elements.ExprEntityName;
import com.mineskript.client.entity.elements.ExprNearestEntity;
import com.mineskript.client.entity.elements.ExprNearestPlayer;
import com.mineskript.client.entity.elements.ExprTargetEntity;
import com.mineskript.client.entity.elements.ExprVehicle;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

/** The entities around you and their properties. */
public final class EntityModule implements SyntaxModule {
    @Override
    public String name() {
        return "entity";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        EntityEvents.register(registry);

        ExprTargetEntity.register(registry);
        ExprNearestEntity.register(registry);
        ExprNearestPlayer.register(registry);
        ExprEntityName.register(registry);
        ExprEntityId.register(registry);
        ExprEntityCoordinate.register(registry);
        ExprEntityDistance.register(registry);
        ExprVehicle.register(registry);
    }
}
