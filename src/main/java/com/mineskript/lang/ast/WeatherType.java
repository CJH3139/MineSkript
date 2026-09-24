package com.mineskript.lang.ast;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum WeatherType {
    CLEAR(List.of("clear", "sun", "sunny")),
    RAIN(List.of("rain", "rainy", "raining")),
    THUNDER(List.of("thunder", "thundering", "thunderstorm"));

    private final List<String> names;

    WeatherType(List<String> names) {
        this.names = names;
    }

    public static Optional<WeatherType> parse(String text) {
        String name = text.strip().toLowerCase(Locale.ROOT);
        for (WeatherType type : values()) {
            if (type.names.contains(name)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static WeatherType of(boolean raining, boolean thundering) {
        if (thundering) {
            return THUNDER;
        }
        return raining ? RAIN : CLEAR;
    }

    @Override
    public String toString() {
        return names.get(0);
    }
}
