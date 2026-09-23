package com.mineskript.game;

import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import java.util.Map;

public record WorldSnapshot(
        boolean hasWorld,
        int blockX,
        int blockY,
        int blockZ,
        boolean onGround,
        boolean sneaking,
        boolean sprinting,
        double health,
        int hunger,
        int xpLevel,
        int selectedSlot,
        boolean usingItem,
        boolean screenOpen,
        boolean raining,
        boolean thundering,
        String gamemode,
        double fallDistance,
        double velocityY,
        List<ItemValue> inventory,
        ItemValue heldItem,
        ItemValue useItem,
        boolean consumingItem,
        int useItemRemaining,
        int totalExperience,
        Map<String, Integer> effects,
        EntityValue vehicle,
        String dimension,
        List<String> onlineNames) {

    public static WorldSnapshot empty() {
        return new WorldSnapshot(false, 0, 0, 0, true, false, false, 0, 0, 0, 0, false, false, false, false, "survival", 0, 0,
                List.of(), ItemValue.empty(), ItemValue.empty(), false, 0, 0, Map.of(), null, "", List.of());
    }
}
