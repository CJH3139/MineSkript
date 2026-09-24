package com.mineskript.lang.ast;

import java.util.Locale;
import java.util.Optional;

public enum GameMode {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR;

    public static Optional<GameMode> parse(String text) {
        String name = text.strip().toLowerCase(Locale.ROOT);
        for (GameMode mode : values()) {
            if (mode.id().equals(name)) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return id();
    }
}
