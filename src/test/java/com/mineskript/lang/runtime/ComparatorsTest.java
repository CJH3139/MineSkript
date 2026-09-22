package com.mineskript.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;
import org.junit.jupiter.api.Test;

class ComparatorsTest {
    @Test
    void parseTimeCapabilities() {
        assertTrue(Comparators.canCompare(SkType.NUMBER, SkType.NUMBER));
        assertTrue(Comparators.canOrder(SkType.NUMBER, SkType.NUMBER));
        assertTrue(Comparators.canCompare(SkType.TEXT, SkType.TEXT));
        assertFalse(Comparators.canOrder(SkType.TEXT, SkType.TEXT));
        assertTrue(Comparators.canCompare(SkType.BLOCK, SkType.BLOCKTYPE));
        assertTrue(Comparators.canCompare(SkType.BLOCKTYPE, SkType.BLOCKTYPE));
        assertTrue(Comparators.canCompare(SkType.BLOCK, SkType.BLOCK));
        assertTrue(Comparators.canCompare(SkType.TIMESPAN, SkType.TIMESPAN));
        assertTrue(Comparators.canOrder(SkType.TIMESPAN, SkType.TIMESPAN));
        assertTrue(Comparators.canCompare(SkType.BOOLEAN, SkType.BOOLEAN));
        assertFalse(Comparators.canCompare(SkType.PLAYER, SkType.BLOCKTYPE));
        assertFalse(Comparators.canCompare(SkType.TEXT, SkType.NUMBER));
        assertTrue(Comparators.canCompare(SkType.OBJECT, SkType.NUMBER));
    }

    @Test
    void relatesNumbersTextBlocksAndTimespans() {
        assertEquals(0, Comparators.relate(5.0, 5.0));
        assertTrue(Comparators.relate(3.0, 5.0) < 0);
        assertTrue(Comparators.relate(9.0, 5.0) > 0);
        assertEquals(0, Comparators.relate("a", "a"));
        assertFalse(Comparators.relate("a", "A") == 0);
        assertEquals(0, Comparators.relate(new BlockValue("minecraft:stone"), new BlockType("minecraft:stone")));
        assertFalse(Comparators.relate(new BlockValue("minecraft:stone"), new BlockType("minecraft:dirt")) == 0);
        assertEquals(0, Comparators.relate(new BlockType("minecraft:stone"), new BlockValue("minecraft:stone")));
        assertTrue(Comparators.relate(new Timespan(1), new Timespan(2)) < 0);
        assertEquals(0, Comparators.relate(true, true));
    }

    @Test
    void refusesIncomparableValues() {
        ScriptError error = assertThrows(ScriptError.class, () -> Comparators.relate("5", 5.0));
        assertEquals("cannot compare text with number", error.getMessage());
    }
}
