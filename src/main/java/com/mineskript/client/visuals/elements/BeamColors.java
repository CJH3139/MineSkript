package com.mineskript.client.visuals.elements;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

final class BeamColors {
    static final int WHITE = 0xF9FFFE;

    private static final Map<String, Integer> DYES = dyes();

    private BeamColors() {
    }

    private static Map<String, Integer> dyes() {
        Map<String, Integer> dyes = new LinkedHashMap<>();
        dyes.put("white", WHITE);
        dyes.put("orange", 0xF9801D);
        dyes.put("magenta", 0xC74EBD);
        dyes.put("light_blue", 0x3AB3DA);
        dyes.put("yellow", 0xFED83D);
        dyes.put("lime", 0x80C71F);
        dyes.put("pink", 0xF38BAA);
        dyes.put("gray", 0x474F52);
        dyes.put("light_gray", 0x9D9D97);
        dyes.put("cyan", 0x169C9C);
        dyes.put("purple", 0x8932B8);
        dyes.put("blue", 0x3C44AA);
        dyes.put("brown", 0x835432);
        dyes.put("green", 0x5E7C16);
        dyes.put("red", 0xB02E26);
        dyes.put("black", 0x1D1D21);
        return Map.copyOf(dyes);
    }

    static OptionalInt rgb(String name) {
        String key = name.trim().toLowerCase(Locale.ROOT).replaceAll("[\\s_]+", "_").replace("grey", "gray");
        if (key.startsWith("#") && key.length() == 7) {
            try {
                return OptionalInt.of(Integer.parseInt(key.substring(1), 16));
            } catch (NumberFormatException error) {
                return OptionalInt.empty();
            }
        }
        Integer dye = DYES.get(key);
        return dye == null ? OptionalInt.empty() : OptionalInt.of(dye);
    }
}
