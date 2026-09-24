package com.mineskript.lang.ast;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record EnchantmentType(Enchantment enchantment, int level) {
    public static final int ANY_LEVEL = -1;

    private static final Pattern WITH_LEVEL = Pattern.compile("(.+?)\\s+(\\d{1,4})");

    public static Optional<EnchantmentType> parse(String text) {
        Matcher matcher = WITH_LEVEL.matcher(text.strip());
        if (matcher.matches()) {
            int level = Integer.parseInt(matcher.group(2));
            return Enchantment.parse(matcher.group(1)).map(enchantment -> new EnchantmentType(enchantment, level));
        }
        return Enchantment.parse(text).map(enchantment -> new EnchantmentType(enchantment, ANY_LEVEL));
    }

    public boolean anyLevel() {
        return level == ANY_LEVEL;
    }

    public boolean matches(String text) {
        return parse(text).map(this::equals).orElse(false) || toString().equalsIgnoreCase(text.strip());
    }

    @Override
    public String toString() {
        return anyLevel() ? enchantment.toString() : enchantment + " " + level;
    }
}
