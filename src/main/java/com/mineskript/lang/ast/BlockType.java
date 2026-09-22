package com.mineskript.lang.ast;

import java.util.Locale;

public record BlockType(String id) {
    public static BlockType fromWords(String words) {
        String path = words.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
        return path.contains(":") ? new BlockType(path) : new BlockType("minecraft:" + path);
    }

    public String path() {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    @Override
    public String toString() {
        return path();
    }
}
