package com.mineskript.lang.ast;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record PotionEffectType(String id) {
    private static final RegistryNames NAMES = new RegistryNames(
            List.of("speed", "slowness", "haste", "mining_fatigue", "strength", "instant_health", "instant_damage",
                    "jump_boost", "nausea", "regeneration", "resistance", "fire_resistance", "water_breathing",
                    "invisibility", "blindness", "night_vision", "hunger", "weakness", "poison", "wither",
                    "health_boost", "absorption", "saturation", "glowing", "levitation", "luck", "unluck",
                    "slow_falling", "conduit_power", "dolphins_grace", "bad_omen", "hero_of_the_village", "darkness",
                    "trial_omen", "raid_omen", "wind_charged", "weaving", "oozing", "infested",
                    "breath_of_the_nautilus"),
            Map.ofEntries(
                    Map.entry("swiftness", "speed"),
                    Map.entry("slow", "slowness"),
                    Map.entry("fast digging", "haste"),
                    Map.entry("fast mining", "haste"),
                    Map.entry("slow digging", "mining_fatigue"),
                    Map.entry("slow mining", "mining_fatigue"),
                    Map.entry("increase damage", "strength"),
                    Map.entry("increased damage", "strength"),
                    Map.entry("health", "instant_health"),
                    Map.entry("damage", "instant_damage"),
                    Map.entry("jump", "jump_boost"),
                    Map.entry("confusion", "nausea"),
                    Map.entry("damage resistance", "resistance"),
                    Map.entry("fire immunity", "fire_resistance"),
                    Map.entry("reduce damage", "weakness"),
                    Map.entry("reduced damage", "weakness"),
                    Map.entry("wither effect", "wither"),
                    Map.entry("wither potion effect", "wither"),
                    Map.entry("max health boost", "health_boost"),
                    Map.entry("maximum health boost", "health_boost"),
                    Map.entry("bad luck", "unluck"),
                    Map.entry("floating", "levitation"),
                    Map.entry("slow fall", "slow_falling")));

    public static Optional<PotionEffectType> parse(String text) {
        return NAMES.id(text).map(PotionEffectType::new);
    }

    public static PotionEffectType fromId(String id) {
        return new PotionEffectType(RegistryNames.normalisedId(id));
    }

    public boolean matches(String text) {
        return parse(text).map(this::equals).orElse(RegistryNames.normalisedId(text).equals(id));
    }

    @Override
    public String toString() {
        return RegistryNames.displayName(id);
    }
}
