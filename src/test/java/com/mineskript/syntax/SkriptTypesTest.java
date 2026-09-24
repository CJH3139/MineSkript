package com.mineskript.syntax;

import static com.mineskript.syntax.SyntaxTestSupport.condition;
import static com.mineskript.syntax.SyntaxTestSupport.eval;
import static com.mineskript.syntax.SyntaxTestSupport.parser;
import static com.mineskript.syntax.SyntaxTestSupport.scope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Enchantment;
import com.mineskript.lang.ast.EnchantmentType;
import com.mineskript.lang.ast.EntityType;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.GameMode;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.PotionEffectType;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.WeatherType;
import com.mineskript.lang.parse.Pattern;
import com.mineskript.lang.runtime.Comparators;
import com.mineskript.lang.runtime.Converters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SkriptTypesTest {
    private final ScriptRunner runner = new ScriptRunner();

    private static ItemValue enchanted(String id, Map<String, Integer> enchantments) {
        return new ItemValue(id, id.substring(id.indexOf(':') + 1).replace('_', ' '), 1, 10, 1561,
                new FakeGameBridge.Details(null, List.of(), enchantments, List.of(), Map.of()));
    }

    private List<String> messages(String body) {
        runner.run("on load:\n" + body.indent(4));
        return runner.game.messages;
    }

    private static String conditionClass(String text) {
        return condition(text, new Event.Load()).getClass().getSimpleName();
    }

    private static String expressionClass(String text) {
        return parser().parse(text, SkType.OBJECT, scope(new Event.Load())).orElseThrow(() -> new AssertionError(text))
                .getClass().getSimpleName();
    }

    @Test
    void literalsOfTheNewTypes() {
        assertEquals(Optional.of(GameMode.CREATIVE), GameMode.parse("Creative"));
        assertEquals(Optional.of(WeatherType.RAIN), WeatherType.parse("raining"));
        assertEquals(Optional.of(WeatherType.CLEAR), WeatherType.parse("sunny"));
        assertEquals(Optional.of(new PotionEffectType("minecraft:night_vision")), PotionEffectType.parse("night vision"));
        assertEquals(Optional.of(new PotionEffectType("minecraft:night_vision")), PotionEffectType.parse("night_vision"));
        assertEquals(Optional.of(new PotionEffectType("minecraft:jump_boost")), PotionEffectType.parse("jump"));
        assertEquals(Optional.of(new PotionEffectType("minecraft:haste")), PotionEffectType.parse("fast digging"));
        assertEquals(Optional.of(new PotionEffectType("mymod:frozen")), PotionEffectType.parse("mymod:frozen"));
        assertEquals(Optional.empty(), PotionEffectType.parse("teleport"));
        assertEquals(Optional.of(new Enchantment("minecraft:luck_of_the_sea")), Enchantment.parse("luck of the sea"));
        assertEquals(Optional.of(new Enchantment("minecraft:vanishing_curse")), Enchantment.parse("curse of vanishing"));
        assertEquals(Optional.of(new EnchantmentType(new Enchantment("minecraft:sharpness"), 5)),
                EnchantmentType.parse("sharpness 5"));
        assertEquals(Optional.of(new EnchantmentType(new Enchantment("minecraft:mending"), -1)),
                EnchantmentType.parse("mending"));
        assertEquals(Optional.of(new EntityType("minecraft:ender_dragon")), EntityType.parse("an ender dragon"));
        assertEquals(Optional.empty(), EntityType.parse("sneaking"));
        assertEquals(Optional.of(new EntityType("minecraft:wolf")), EntityType.parse("dog"));
        assertEquals("night vision", new PotionEffectType("minecraft:night_vision").toString());
        assertEquals("fire aspect 2", new EnchantmentType(new Enchantment("minecraft:fire_aspect"), 2).toString());
    }

    @Test
    void itemTypeIsTheSkriptNameOfBlockType() {
        assertEquals(SkType.BLOCKTYPE, Pattern.typeNamed("itemtypes"));
        assertEquals(SkType.BLOCKTYPE, Pattern.typeNamed("item type"));
        assertEquals(SkType.BLOCKTYPE, Pattern.typeNamed("block type"));
        assertEquals(SkType.GAMEMODE, Pattern.typeNamed("game mode"));
        assertEquals(SkType.POTIONEFFECTTYPE, Pattern.typeNamed("potion effect type"));
        assertEquals("item type", Converters.typeName(SkType.BLOCKTYPE));
        runner.run("""
                function first(i: item type, b: block type) :: text:
                    return "%{_i}% %{_b}%"
                on load:
                    send first(diamond, stone)
                """);
        assertEquals(List.of("diamond stone"), runner.game.messages);
    }

    @Test
    void gamemodeIsATypeAndStillMatchesText() {
        runner.game.gamemode = "creative";
        assertEquals(List.of("a", "b", "c", "d", "e", "creative", "f"), messages("""
                if player's gamemode is creative:
                    send "a"
                if gamemode is creative:
                    send "b"
                if gamemode is "creative":
                    send "c"
                if game mode of player is survival or creative:
                    send "d"
                if gamemode of me is not spectator:
                    send "e"
                send "%gamemode%"
                set {_g} to gamemode
                if {_g} is "CREATIVE":
                    send "f"
                """));
        assertEquals(GameMode.CREATIVE, eval("player's game mode", SkType.OBJECT, runner.game));
    }

    @Test
    void potionEffectConditionsAndTier() {
        runner.game.effects.put("minecraft:speed", 2);
        runner.game.effects.put("minecraft:poison", 1);
        assertEquals(List.of("a", "b", "c", "d", "e", "2", "0", "2"), messages("""
                if player has potion speed:
                    send "a"
                if player has any potion effects:
                    send "b"
                if player doesn't have potion effects speed and night vision:
                    send "c"
                if player has potion swiftness or haste:
                    send "d"
                if player is poisoned:
                    send "e"
                send "%tier of speed of player%"
                send "%the potion amplifier of haste for me%"
                send "%level of effect "speed"%"
                """));
        runner.game.effects.clear();
        runner.game.messages.clear();
        assertEquals(List.of("none", "not poisoned"), messages("""
                if player doesn't have any potion effect:
                    send "none"
                if player is not poisoned:
                    send "not poisoned"
                """));
    }

    @Test
    void enchantmentLevelsTypesAndIsEnchanted() {
        runner.game.setSlot(0, enchanted("minecraft:diamond_sword", Map.of("minecraft:sharpness", 5,
                "minecraft:fire_aspect", 2)));
        runner.game.setSlot(1, new ItemValue("minecraft:stick", "stick", 1, 0, 0));
        assertEquals(List.of("5", "5", "5", "2", "0", "5", "fire aspect 2 and sharpness 5", "a", "b", "c", "d", "e",
                "f", "g", "h"), messages("""
                send "%level of sharpness of held item%"
                send "%held item's sharpness level%"
                send "%sharpness enchantment level on held item%"
                send "%held item's enchantment level of fire aspect%"
                send "%level of mending of held item%"
                send "%level of enchantment "sharpness" on held item%"
                send "%enchantments of held item%"
                if held item is enchanted:
                    send "a"
                if held item is enchanted with sharpness:
                    send "b"
                if held item is enchanted with sharpness 5:
                    send "c"
                if held item is not enchanted with sharpness 4:
                    send "d"
                if held item is enchanted with sharpness 3 or better:
                    send "e"
                if held item is enchanted with sharpness 6 or lower:
                    send "f"
                if held item is enchanted with sharpness and fire aspect 2:
                    send "g"
                if item in slot 1 is not enchanted:
                    send "h"
                """));
        runner.game.messages.clear();
        assertEquals(List.of("has fire aspect 2 as text", "has sharpness 5"), messages("""
                loop enchantments of held item:
                    if loop-value is sharpness 5:
                        send "has sharpness 5"
                    if loop-value is "fire aspect 2":
                        send "has fire aspect 2 as text"
                """));
    }

    @Test
    void hasWithAmountsAndInventories() {
        runner.game.setSlot(0, new ItemValue("minecraft:diamond", "diamond", 3, 0, 0));
        runner.game.setSlot(5, new ItemValue("minecraft:stone", "stone", 64, 0, 0));
        runner.game.setSlot(6, new ItemValue("minecraft:torch", "torch", 2, 0, 0));
        assertEquals(List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "3", "67", "3", "3 diamond"), messages("""
                if player has 3 diamonds:
                    send "a"
                if player doesn't have 4 diamonds:
                    send "b"
                if player has 64 of stone:
                    send "c"
                if player's inventory has diamond:
                    send "d"
                if player has stone in their inventory:
                    send "e"
                if player's inventory contains 2 torches:
                    send "f"
                if inventory of player doesn't contain dirt:
                    send "g"
                if player has diamond and stone:
                    send "h"
                if player has dirt or torch:
                    send "i"
                if player has diamond:
                    send "j"
                send "%amount of diamonds in player's inventory%"
                send "%number of diamond and stone of inventory of player%"
                send "%number of diamond in inventory%"
                send "%slot 0 of player's inventory%"
                """));
    }

    @Test
    void listContainsStillWinsOverInventoryContains() {
        assertEquals("Membership", conditionClass("{_list::*} contains stone"));
        assertEquals("CondHasItem", conditionClass("player's inventory contains stone"));
        assertEquals("CondHasItem", conditionClass("player has 3 diamonds"));
        assertEquals("CondHasEffect", conditionClass("player has effect \"speed\""));
        assertEquals("CondHasPotion", conditionClass("player has potion effect speed"));
        assertEquals("CondIsEnchanted", conditionClass("held item is enchanted"));
        assertEquals("CondInventoryFull", conditionClass("player's inventory is full"));
        assertEquals("CondInventoryFull", conditionClass("inventory is full"));
    }

    @Test
    void newPropertyFormsDoNotTakeOverOldExpressions() {
        assertEquals("ExprXpLevel", expressionClass("level of player"));
        assertEquals("ExprEffectLevel", expressionClass("level of effect \"speed\""));
        assertEquals("ExprPotionEffectTier", expressionClass("level of speed of player"));
        assertEquals("ExprEnchantmentLevel", expressionClass("level of sharpness of held item"));
        assertEquals("ExprBlockAt", expressionClass("block at {_spot}"));
        assertEquals("ExprBlock", expressionClass("block above target block"));
        assertEquals("ExprBlock", expressionClass("block above player"));
        assertEquals("ExprHeldItem", expressionClass("player's tool"));
        assertEquals("ExprInventoryCount", expressionClass("number of diamond in inventory"));
        assertEquals("ExprInventoryCount", expressionClass("amount of diamond in player's inventory"));
        assertEquals("ExprItemInSlot", expressionClass("slot 3 of player's inventory"));
        assertEquals("ExprItemMaxDamage", expressionClass("max durability of held item"));
        assertEquals("ExprDurability", expressionClass("durability of held item"));
    }

    @Test
    void playerPropertyForms() {
        FakeGameBridge game = runner.game;
        game.xpLevel = 7;
        game.xpProgress = 0.5;
        game.totalExperience = 120;
        game.air = 60;
        game.saturation = 3;
        game.hunger = 15;
        game.maxHealth = 24;
        game.yaw = 90;
        game.pitch = 10;
        game.selected = 2;
        game.dimension = "minecraft:the_nether";
        game.biome = "minecraft:nether_wastes";
        assertEquals(List.of("7", "7", "0.5", "120", "120", "60 ticks", "300", "3", "15", "15", "15", "24", "24",
                "90", "10", "2", "2", "minecraft:the_nether", "minecraft:the_nether", "minecraft:nether_wastes",
                "15", "15"), messages("""
                send "%player's level%"
                send "%level of player%"
                send "%player's level progress%"
                send "%experience of player%"
                send "%player's total experience%"
                send "%player's remaining air%"
                send "%max air of player%"
                send "%player's saturation%"
                send "%food level%"
                send "%player's hunger bar%"
                send "%food meter of me%"
                send "%maximum health of player%"
                send "%player's max health%"
                send "%player's yaw%"
                send "%pitch of player%"
                send "%player's hotbar slot%"
                send "%current hotbar slot%"
                send "%world of player%"
                send "%player's world%"
                send "%biome of player%"
                send "%light level of player%"
                send "%sky light level of player%"
                """));
    }

    @Test
    void settablePropertyForms() {
        runner.run("""
                on load:
                    set player's hotbar slot to 3
                    set player's yaw to 45
                    add 5 to pitch of player
                """);
        assertEquals(3, runner.game.selected);
        assertEquals(45.0, runner.game.yaw);
        assertEquals(5.0, runner.game.pitch);
    }

    @Test
    void locationPropertyForms() {
        FakeGameBridge game = runner.game;
        game.setBlockAt(10, 65, 10, "minecraft:gold_block");
        game.biomes.put(FakeGameBridge.blockKey(10, 64, 10), "minecraft:desert");
        game.blockLight.put(FakeGameBridge.blockKey(10, 64, 10), 7);
        game.skyLightAt.put(FakeGameBridge.blockKey(10, 64, 10), 4);
        assertEquals(List.of("gold_block", "minecraft:desert", "7", "7", "4", "4", "minecraft:the_end", "a", "b"),
                messages("""
                set {_spot} to location(10.5, 64, 10.5)
                send "%block above {_spot}%"
                send "%biome of {_spot}%"
                send "%light level of {_spot}%"
                send "%block light level of {_spot}%"
                send "%sky light level of {_spot}%"
                send "%sunlight level of {_spot}%"
                send "%world of location(0, 0, 0, "the_end")%"
                if biome of location(0, 0, 0, "the_end") is not set:
                    send "a"
                if block north of location(0, 0, 0, "the_end") is not set:
                    send "b"
                """));
    }

    @Test
    void itemAndEntityPropertyForms() {
        FakeGameBridge game = runner.game;
        game.setSlot(0, new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 1, 61, 1561));
        game.offhand = new ItemValue("minecraft:torch", "torch", 7, 0, 0);
        game.vehicle = new EntityValue("minecraft:horse", "Horse", 0, 64, 0, 1);
        game.targetEntity = new EntityValue("minecraft:zombie", "Zombie", 0, 64, 2, 2);
        assertEquals(List.of("1500", "1500", "1561", "1561", "diamond pickaxe", "diamond pickaxe", "7 torch",
                "7 torch", "Horse", "Zombie", "a", "b", "c"), messages("""
                send "%durability of held item%"
                send "%held item's durability%"
                send "%max durability of held item%"
                send "%held item's maximum damage%"
                send "%player's weapon%"
                send "%tool of player%"
                send "%player's offhand item%"
                send "%off hand tool of player%"
                send "%player's vehicle%"
                send "%target of player%"
                if player's target is zombie:
                    send "a"
                if target entity of me is not creeper:
                    send "b"
                if player's vehicle is horse:
                    send "c"
                """));
    }

    @Test
    void specificErrorsSurviveSkippingPatternsThatCannotMatch() {
        assertEquals(List.of("t.ms:2: loop-index is only available inside a loop"),
                runner.errorsOf("on load:\n    send \"%loop-index above location(1, 2, 3)%\"\n"));
        assertEquals(List.of("t.ms:2: event-block is not available in this event"),
                runner.errorsOf("on load:\n    send \"%event-block's amount%\"\n"));
    }

    @Test
    void comparingNamedValuesWithTextAndEntities() {
        assertTrue(Comparators.canCompare(SkType.GAMEMODE, SkType.TEXT));
        assertTrue(Comparators.canCompare(SkType.ENTITY, SkType.ENTITYTYPE));
        assertFalse(Comparators.canCompare(SkType.GAMEMODE, SkType.NUMBER));
        assertEquals(0, Comparators.relate(new PotionEffectType("minecraft:night_vision"), "night vision"));
        assertEquals(0, Comparators.relate("NIGHT_VISION", new PotionEffectType("minecraft:night_vision")));
        assertEquals(0, Comparators.relate(new EntityValue("minecraft:zombie", "Zombie", 0, 0, 0, 0),
                new EntityType("minecraft:zombie")));
        assertEquals(1, Comparators.relate(GameMode.CREATIVE, "survival"));
    }
}
