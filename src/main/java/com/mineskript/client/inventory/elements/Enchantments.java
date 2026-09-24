package com.mineskript.client.inventory.elements;

import java.util.Locale;

final class Enchantments {
    private static final String VANILLA = "minecraft:";

    private Enchantments() {
    }

    static String displayName(String id) {
        return id.startsWith(VANILLA) ? id.substring(VANILLA.length()).replace('_', ' ') : id;
    }

    static String id(String name) {
        String trimmed = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
        return trimmed.contains(":") ? trimmed : VANILLA + trimmed;
    }
}
