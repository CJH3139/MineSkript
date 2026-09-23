package com.mineskript.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConvertersTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final Context context = new Context(game, "t.ms", Map.of());

    @Test
    void formatsEveryValueKind() {
        assertEquals("hi", Converters.toText("hi", context));
        assertEquals("64", Converters.toText(64.0, context));
        assertEquals("1.5", Converters.toText(1.5, context));
        assertEquals("-3", Converters.toText(-3.0, context));
        assertEquals("true", Converters.toText(true, context));
        assertEquals("20 ticks", Converters.toText(new Timespan(20), context));
        assertEquals("oak_log", Converters.toText(new BlockType("minecraft:oak_log"), context));
        assertEquals("stone", Converters.toText(new BlockValue("minecraft:stone"), context));
        assertEquals("Steve", Converters.toText(PlayerRef.LOCAL, context));
    }

    @Test
    void joinsListsLikeSkript() {
        assertEquals("a, b and c", Converters.toText(List.of("a", "b", "c"), context));
        assertEquals("a and b", Converters.toText(List.of("a", "b"), context));
        assertEquals("a", Converters.toText(List.of("a"), context));
    }

    @Test
    void knowsWhichConversionsExist() {
        assertTrue(Converters.canConvert(SkType.NUMBER, SkType.NUMBER));
        assertTrue(Converters.canConvert(SkType.NUMBER, SkType.OBJECT));
        assertTrue(Converters.canConvert(SkType.NUMBER, SkType.TEXT));
        assertTrue(Converters.canConvert(SkType.BLOCK, SkType.BLOCKTYPE));
        assertFalse(Converters.canConvert(SkType.TEXT, SkType.NUMBER));
        assertFalse(Converters.canConvert(SkType.BLOCKTYPE, SkType.BLOCK));
    }

    @Test
    void convertsBlockToBlockTypeAndAnythingToText() {
        assertEquals(new BlockType("minecraft:stone"), Converters.convert(new BlockValue("minecraft:stone"), SkType.BLOCKTYPE, context));
        assertEquals("7", Converters.convert(7.0, SkType.TEXT, context));
        assertEquals(7.0, Converters.convert(7.0, SkType.OBJECT, context));
        assertEquals(List.of(new BlockType("minecraft:stone")), Converters.convert(List.of(new BlockValue("minecraft:stone")), SkType.BLOCKTYPE, context));
    }

    @Test
    void formatsLargeWholeNumbersWithoutTruncatingToLongRange() {
        String text = Converters.toText(1.0e20, context);
        assertFalse(text.contains("9223372036854775807"));
    }

    @Test
    void playerRefToTextRequiresWorld() {
        FakeGameBridge noWorld = new FakeGameBridge();
        noWorld.hasWorld = false;
        Context noWorldContext = new Context(noWorld, "t.ms", Map.of());
        assertThrows(ScriptError.class, () -> Converters.toText(PlayerRef.LOCAL, noWorldContext));
    }

    @Test
    void noneRendersAndConvertsOnlyToText() {
        assertEquals("<none>", Converters.toText(None.NONE, context));
        assertEquals("<none>", Converters.convert(None.NONE, SkType.TEXT, context));
        assertEquals(None.NONE, Converters.convert(None.NONE, SkType.OBJECT, context));
        ScriptError error = assertThrows(ScriptError.class, () -> Converters.convert(None.NONE, SkType.NUMBER, context));
        assertEquals("variable is not set", error.getMessage());
        assertEquals(SkType.OBJECT, Converters.typeOf(None.NONE));
    }

    @Test
    void rendersItemsAndEntities() {
        assertEquals("diamond pickaxe", Converters.toText(new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 1, 0, 1561), context));
        assertEquals("64 cobblestone", Converters.toText(new ItemValue("minecraft:cobblestone", "cobblestone", 64, 0, 0), context));
        assertEquals("air", Converters.toText(ItemValue.empty(), context));
        assertEquals("Zombie", Converters.toText(new EntityValue("minecraft:zombie", "Zombie", 1.0, 2.0, 3.0, 4.5), context));
        assertEquals(SkType.ITEM, Converters.typeOf(ItemValue.empty()));
        assertEquals(SkType.ENTITY, Converters.typeOf(new EntityValue("minecraft:zombie", "Zombie", 0, 0, 0, 0)));
    }

    @Test
    void itemsConvertToBlockTypesAndText() {
        ItemValue stone = new ItemValue("minecraft:stone", "stone", 3, 0, 0);
        assertTrue(Converters.canConvert(SkType.ITEM, SkType.BLOCKTYPE));
        assertTrue(Converters.canConvert(SkType.ITEM, SkType.TEXT));
        assertFalse(Converters.canConvert(SkType.ITEM, SkType.NUMBER));
        assertEquals(new BlockType("minecraft:stone"), Converters.convert(stone, SkType.BLOCKTYPE, context));
    }
}
