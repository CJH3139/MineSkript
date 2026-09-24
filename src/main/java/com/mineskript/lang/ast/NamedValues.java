package com.mineskript.lang.ast;

import java.util.Optional;

public final class NamedValues {
    private NamedValues() {
    }

    public static Optional<Object> parse(String words, SkType type) {
        return switch (type) {
            case GAMEMODE -> GameMode.parse(words).map(Object.class::cast);
            case WEATHERTYPE -> WeatherType.parse(words).map(Object.class::cast);
            case POTIONEFFECTTYPE -> PotionEffectType.parse(words).map(Object.class::cast);
            case ENCHANTMENT -> Enchantment.parse(words).map(Object.class::cast);
            case ENCHANTMENTTYPE -> EnchantmentType.parse(words).map(Object.class::cast);
            case ENTITYTYPE -> EntityType.parse(words).map(Object.class::cast);
            default -> Optional.empty();
        };
    }

    public static boolean isNamed(SkType type) {
        return switch (type) {
            case GAMEMODE, WEATHERTYPE, POTIONEFFECTTYPE, ENCHANTMENT, ENCHANTMENTTYPE, ENTITYTYPE -> true;
            default -> false;
        };
    }
}
