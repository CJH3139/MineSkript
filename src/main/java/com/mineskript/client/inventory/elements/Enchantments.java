package com.mineskript.client.inventory.elements;

import java.util.Locale;

final class Enchantments {
    private static final String VANILLA = "minecraft:";

    private Enchantments() {
    }

    static String id(String name) {
        String trimmed = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
        return trimmed.contains(":") ? trimmed : VANILLA + trimmed;
    }
}
