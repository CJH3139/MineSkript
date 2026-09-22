package com.mineskript.lang.ast;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record Timespan(int ticks) {
    private static final Map<String, Double> MILLIS_PER_UNIT = Map.ofEntries(
            Map.entry("tick", 50.0),
            Map.entry("ticks", 50.0),
            Map.entry("millisecond", 1.0),
            Map.entry("milliseconds", 1.0),
            Map.entry("ms", 1.0),
            Map.entry("second", 1000.0),
            Map.entry("seconds", 1000.0),
            Map.entry("sec", 1000.0),
            Map.entry("secs", 1000.0),
            Map.entry("minute", 60000.0),
            Map.entry("minutes", 60000.0),
            Map.entry("min", 60000.0),
            Map.entry("mins", 60000.0),
            Map.entry("hour", 3600000.0),
            Map.entry("hours", 3600000.0));

    public static Optional<Timespan> parse(double amount, String unit) {
        Double millis = MILLIS_PER_UNIT.get(unit.toLowerCase(Locale.ROOT));
        if (millis == null) {
            return Optional.empty();
        }
        int ticks = (int) Math.round(amount * millis / 50.0);
        return Optional.of(new Timespan(Math.max(1, ticks)));
    }

    @Override
    public String toString() {
        return ticks + (ticks == 1 ? " tick" : " ticks");
    }
}
