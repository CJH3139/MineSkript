package com.mineskript.lang.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TimespanTest {
    @Test
    void convertsUnitsToTicksAtFiftyMillisPerTick() {
        assertEquals(1, Timespan.parse(1, "tick").orElseThrow().ticks());
        assertEquals(5, Timespan.parse(5, "ticks").orElseThrow().ticks());
        assertEquals(40, Timespan.parse(2, "seconds").orElseThrow().ticks());
        assertEquals(30, Timespan.parse(1.5, "seconds").orElseThrow().ticks());
        assertEquals(10, Timespan.parse(500, "milliseconds").orElseThrow().ticks());
        assertEquals(1200, Timespan.parse(1, "minute").orElseThrow().ticks());
        assertEquals(72000, Timespan.parse(1, "hour").orElseThrow().ticks());
    }

    @Test
    void neverGoesBelowOneTick() {
        assertEquals(1, Timespan.parse(0, "ticks").orElseThrow().ticks());
        assertEquals(1, Timespan.parse(3, "milliseconds").orElseThrow().ticks());
    }

    @Test
    void rejectsUnknownUnits() {
        assertTrue(Timespan.parse(1, "fortnights").isEmpty());
    }

    @Test
    void blockTypeNormalisesWordsToIds() {
        assertEquals("minecraft:oak_log", BlockType.fromWords("oak log").id());
        assertEquals("minecraft:stone", BlockType.fromWords("Stone").id());
        assertEquals("oak_log", BlockType.fromWords("oak log").path());
    }
}
