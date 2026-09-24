package com.mineskript.lang.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class RegistryNames {
    private static final String VANILLA = "minecraft:";
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private final Map<String, String> names = new HashMap<>();

    RegistryNames(List<String> ids, Map<String, String> aliases) {
        for (String id : ids) {
            names.put(id.replace('_', ' '), VANILLA + id);
        }
        aliases.forEach((alias, id) -> names.put(alias, VANILLA + id));
    }

    Optional<String> id(String text) {
        String name = text.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (name.contains(":")) {
            return ID.matcher(name).matches() ? Optional.of(name) : Optional.empty();
        }
        return Optional.ofNullable(names.get(name.replace('_', ' ')));
    }

    static String displayName(String id) {
        return id.startsWith(VANILLA) ? id.substring(VANILLA.length()).replace('_', ' ') : id;
    }

    static String normalisedId(String text) {
        String name = text.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
        return name.contains(":") ? name : VANILLA + name;
    }
}
