package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.ItemDetails;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.script.EventDispatcher;
import com.mineskript.script.ScriptRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ItemDataTest {
    private static final FakeGameBridge.Details SWORD_DETAILS = new FakeGameBridge.Details("Excalibur",
            List.of("Forged in fire", "Soulbound"),
            Map.of("minecraft:fire_aspect", 2, "minecraft:sharpness", 5, "mymod:frost", 1),
            List.of(7.0, 2.5),
            Map.of("minecraft:rarity", "epic", "minecraft:custom_data", "{tier:3}"));

    private final FakeGameBridge game = new FakeGameBridge();

    ItemDataTest() {
        game.setSlot(0, new ItemValue("minecraft:diamond_sword", "Excalibur", 1, 0, 1561, SWORD_DETAILS));
        game.setSlot(1, new ItemValue("minecraft:stone", "Stone", 64, 0, 0));
    }

    private Object eval(String text, SkType type) {
        return SyntaxTestSupport.eval(text, type, game);
    }

    @Test
    void customNameIsNoneUnlessRenamed() {
        assertEquals("Excalibur", eval("custom name of held item", SkType.TEXT));
        assertEquals("Excalibur", eval("held item's custom name", SkType.TEXT));
        assertEquals(None.NONE, eval("custom name of item in slot 1", SkType.TEXT));
        assertEquals("Stone", eval("name of item in slot 1", SkType.TEXT));
    }

    @Test
    void loreIsAListOfLines() {
        assertEquals(List.of("Forged in fire", "Soulbound"), eval("lore of held item", SkType.TEXT));
        assertEquals(List.of(), eval("item in slot 1's lore", SkType.TEXT));
        assertTrue(SyntaxTestSupport.expr("lore of held item", SkType.TEXT, null).isList());
    }

    @Test
    void enchantmentsReadAsNameAndLevel() {
        assertEquals(List.of("fire aspect 2", "sharpness 5", "mymod:frost 1"), eval("enchantments of held item", SkType.TEXT));
        assertEquals(List.of(), eval("enchantments of item in slot 1", SkType.TEXT));
    }

    @Test
    void enchantmentLevelAcceptsNamesAndIds() {
        assertEquals(5.0, eval("level of enchantment \"sharpness\" on held item", SkType.NUMBER));
        assertEquals(2.0, eval("enchantment level of \"Fire Aspect\" on held item", SkType.NUMBER));
        assertEquals(2.0, eval("level of enchantment \"minecraft:fire_aspect\" on held item", SkType.NUMBER));
        assertEquals(1.0, eval("level of enchantment \"mymod:frost\" on held item", SkType.NUMBER));
        assertEquals(0.0, eval("level of enchantment \"mending\" on held item", SkType.NUMBER));
        assertEquals(0.0, eval("level of enchantment \"sharpness\" on item in slot 1", SkType.NUMBER));
    }

    @Test
    void customModelDataIsTheFirstNumberOrNone() {
        assertEquals(7.0, eval("custom model data of held item", SkType.NUMBER));
        assertEquals(None.NONE, eval("custom model data of item in slot 1", SkType.NUMBER));
    }

    @Test
    void anyComponentReadsAsText() {
        assertEquals("epic", eval("component \"minecraft:rarity\" of held item", SkType.TEXT));
        assertEquals("epic", eval("data component \"Rarity\" of held item", SkType.TEXT));
        assertEquals("{tier:3}", eval("the component \"custom_data\" of held item", SkType.TEXT));
        assertEquals(None.NONE, eval("component \"minecraft:lore\" of held item", SkType.TEXT));
        assertEquals(None.NONE, eval("component \"rarity\" of item in slot 1", SkType.TEXT));
    }

    @Test
    void theExpressionsWorkInScripts() {
        ScriptRunner runner = new ScriptRunner();
        runner.game.setSlot(0, new ItemValue("minecraft:diamond_sword", "Excalibur", 1, 0, 1561, SWORD_DETAILS));
        runner.run("on load:\n"
                + "    if lore of held item contains \"Soulbound\":\n        send \"bound\"\n"
                + "    loop enchantments of held item:\n        send loop-value\n"
                + "    if custom name of offhand item is not set:\n        send \"plain offhand\"\n");
        assertEquals(List.of("bound", "fire aspect 2", "sharpness 5", "mymod:frost 1", "plain offhand"), runner.game.messages);
    }

    @Test
    void aNonItemStopsTheLine() {
        ScriptRunner runner = new ScriptRunner();
        runner.run("on load:\n    send \"%lore of {_nothing}%\"\n");
        assertEquals(1, runner.game.errors.size());
    }

    @Test
    void detailsDoNotChangeEqualityOrTheInventoryDiff() {
        ItemValue plain = new ItemValue("minecraft:diamond_sword", "Excalibur", 1, 0, 1561);
        ItemValue detailed = new ItemValue("minecraft:diamond_sword", "Excalibur", 1, 0, 1561, SWORD_DETAILS);
        assertEquals(plain, detailed);
        assertEquals(plain.hashCode(), detailed.hashCode());
        assertEquals(plain.toString(), detailed.toString());
        assertEquals(ItemDetails.NONE, plain.details());
        assertNotEquals(plain, new ItemValue("minecraft:diamond_sword", "Excalibur", 2, 0, 1561));

        ScriptRegistry registry = new ScriptRegistry();
        EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler());
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", "on inventory change:\n    send \"changed\"\n");
        registry.replace(List.of(script));
        game.setSlot(2, plain);
        dispatcher.tick();
        dispatcher.tick();
        game.setSlot(2, detailed);
        dispatcher.tick();
        assertEquals(List.of(), game.messages);
    }
}
