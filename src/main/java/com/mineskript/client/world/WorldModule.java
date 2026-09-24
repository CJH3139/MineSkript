package com.mineskript.client.world;

import com.mineskript.client.world.elements.CondWeather;
import com.mineskript.client.world.elements.ExprBiome;
import com.mineskript.client.world.elements.ExprBlock;
import com.mineskript.client.world.elements.ExprBlockAt;
import com.mineskript.client.world.elements.ExprDifficulty;
import com.mineskript.client.world.elements.ExprDimension;
import com.mineskript.client.world.elements.ExprGameTime;
import com.mineskript.client.world.elements.ExprLightLevel;
import com.mineskript.client.world.elements.ExprSkyLight;
import com.mineskript.client.world.elements.ExprTargetBlock;
import com.mineskript.client.world.elements.ExprTargetCoordinate;
import com.mineskript.client.world.elements.ExprTimeOfDay;
import com.mineskript.client.world.elements.WorldEvents;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

/** The world around you: blocks, light, biome, time, weather and dimension. */
public final class WorldModule implements SyntaxModule {
    @Override
    public String name() {
        return "world";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        WorldEvents.register(registry);

        ExprDimension.register(registry);
        ExprGameTime.register(registry);
        ExprTimeOfDay.register(registry);
        ExprDifficulty.register(registry);
        ExprTargetBlock.register(registry);
        ExprBlock.register(registry);
        ExprBiome.register(registry);
        ExprLightLevel.register(registry);
        ExprSkyLight.register(registry);
        ExprBlockAt.register(registry);
        ExprTargetCoordinate.register(registry);

        CondWeather.register(registry);
    }
}
