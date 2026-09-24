package com.mineskript.lang.ast;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Enchantment(String id) {
    private static final RegistryNames NAMES = new RegistryNames(
            List.of("protection", "fire_protection", "feather_falling", "blast_protection", "projectile_protection",
                    "respiration", "aqua_affinity", "thorns", "depth_strider", "frost_walker", "binding_curse",
                    "soul_speed", "swift_sneak", "sharpness", "smite", "bane_of_arthropods", "knockback",
                    "fire_aspect", "looting", "sweeping_edge", "efficiency", "silk_touch", "unbreaking", "fortune",
                    "power", "punch", "flame", "infinity", "luck_of_the_sea", "lure", "loyalty", "impaling",
                    "riptide", "channeling", "multishot", "quick_charge", "piercing", "mending", "vanishing_curse",
                    "density", "breach", "wind_burst", "lunge"),
            Map.ofEntries(
                    Map.entry("curse of binding", "binding_curse"),
                    Map.entry("curse of vanishing", "vanishing_curse"),
                    Map.entry("sweeping", "sweeping_edge"),
                    Map.entry("channelling", "channeling"),
                    Map.entry("multi-shot", "multishot")));

    public static Optional<Enchantment> parse(String text) {
        return NAMES.id(text).map(Enchantment::new);
    }

    public static Enchantment fromId(String id) {
        return new Enchantment(RegistryNames.normalisedId(id));
    }

    public boolean matches(String text) {
        return parse(text).map(this::equals).orElse(RegistryNames.normalisedId(text).equals(id));
    }

    @Override
    public String toString() {
        return RegistryNames.displayName(id);
    }
}
