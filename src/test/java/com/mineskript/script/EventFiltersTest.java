package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.BlockChange;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameSignals;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.EventFilter;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EventFiltersTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000),
            new Scheduler(), new Variables(), () -> {
            });

    private List<String> load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        registry.replace(List.of(script));
        return script.errors().stream().map(Object::toString).toList();
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    private static Event.State state(String header) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", header + ":\n    stop\n");
        assertEquals(List.of(), script.errors(), header);
        return (Event.State) script.triggers().get(0).event();
    }

    private static EntityValue entity(String id) {
        return new EntityValue(id, id, 0, 64, 0, 3);
    }

    @Test
    void skriptEventNamesAreAliasesOfTheExistingEvents() {
        Map<String, String> aliases = Map.ofEntries(
                Map.entry("on break", "block break"),
                Map.entry("on mine", "block break"),
                Map.entry("on block mining", "block break"),
                Map.entry("on break of block", "block break"),
                Map.entry("on place", "block place"),
                Map.entry("on building", "block place"),
                Map.entry("on sneak toggle", "sneak toggle"),
                Map.entry("on toggle sneak", "sneak toggle"),
                Map.entry("on player toggling sneak", "sneak toggle"),
                Map.entry("on sprint toggle", "sprint toggle"),
                Map.entry("on toggle sprint", "sprint toggle"),
                Map.entry("on death of zombie", "entity death"),
                Map.entry("on death", "death"),
                Map.entry("on spawn", "entity spawn"),
                Map.entry("on spawning of creeper", "entity spawn"),
                Map.entry("on join", "join"),
                Map.entry("on login", "join"),
                Map.entry("on player join", "player join"),
                Map.entry("on quit", "leave"),
                Map.entry("on log out", "leave"),
                Map.entry("on tool change", "held"),
                Map.entry("on player's held item change", "held"),
                Map.entry("on player item held changing", "held"),
                Map.entry("on player level change", "level"),
                Map.entry("on level progress change", "xp"),
                Map.entry("on food bar change", "hunger"),
                Map.entry("on player jump", "jump"),
                Map.entry("on player respawn", "respawn"),
                Map.entry("on healing", "heal"),
                Map.entry("on mounting", "mount"),
                Map.entry("on player world change", "dimension"),
                Map.entry("on eat of bread", "consume"),
                Map.entry("on player drinking", "consume"),
                Map.entry("on player tool breaking", "item break"),
                Map.entry("on end item use", "use stop"),
                Map.entry("on chunk loading", "chunk load"),
                Map.entry("on game mode change to creative", "gamemode"),
                Map.entry("on weather change to sunny", "weather"));
        aliases.forEach((header, name) -> assertEquals(name, state(header).name(), header));
    }

    @Test
    void theOldSpellingsKeepMatchingEverything() {
        for (String header : List.of("on block break", "on break block", "on break of block", "on block place",
                "on placing of block", "on gamemode change", "on weather change", "on entity death",
                "on entity spawn", "on consume", "on item break", "on effect gain")) {
            assertEquals(EventFilter.ANY, state(header).filter(), header);
        }
    }

    @Test
    void breakOfFiresOnlyForTheListedBlocks() {
        assertEquals(List.of(), load("""
                on break of stone:
                    send "stone"
                on mine of diamond ore or deepslate diamond ore:
                    send "diamond %event-block%"
                on break:
                    send "any %event-block%"
                """));
        dispatcher.onBlockBreak(new BlockChange("minecraft:stone", 0, 60, 0));
        dispatcher.onBlockBreak(new BlockChange("minecraft:deepslate_diamond_ore", 0, 10, 0));
        dispatcher.onBlockBreak(new BlockChange("minecraft:dirt", 0, 63, 0));
        assertEquals(List.of("stone", "any stone", "diamond deepslate_diamond_ore", "any deepslate_diamond_ore",
                "any dirt"), game.messages);
    }

    @Test
    void placeOfFiresOnlyForTheListedBlocks() {
        load("on place of torch:\n    send \"torch\"\non block place:\n    send \"placed\"\n");
        dispatcher.onBlockPlace(new BlockChange("minecraft:torch", 0, 64, 0));
        dispatcher.onBlockPlace(new BlockChange("minecraft:stone", 0, 64, 0));
        assertEquals(List.of("torch", "placed", "placed"), game.messages);
    }

    @Test
    void gamemodeChangeToAMode() {
        load("""
                on gamemode change to creative:
                    send "creative"
                on gamemode change:
                    send "now %event-gamemode%"
                    if event-gamemode is spectator:
                        send "spectating"
                """);
        ticks(1);
        game.gamemode = "creative";
        ticks(1);
        game.gamemode = "spectator";
        ticks(1);
        assertEquals(List.of("creative", "now creative", "now spectator", "spectating"), game.messages);
    }

    @Test
    void weatherChangeToAWeather() {
        load("""
                on weather change to rain:
                    send "rain"
                on weather change to thunder or clear:
                    send "thunder or clear"
                on weather change:
                    send "weather %event-weather%"
                """);
        ticks(1);
        game.raining = true;
        ticks(1);
        game.thundering = true;
        ticks(1);
        game.raining = false;
        game.thundering = false;
        ticks(1);
        assertEquals(List.of("rain", "weather rain", "thunder or clear", "weather thunder", "thunder or clear",
                "weather clear"), game.messages);
    }

    @Test
    void deathAndSpawnOfEntityTypes() {
        load("""
                on death of zombie:
                    send "zombie died"
                on death of a wither or ender dragon:
                    send "boss died"
                on entity death:
                    send "%event-entity% died"
                on spawn of creeper:
                    send "creeper"
                on spawn:
                    send "spawned %event-entity%"
                on death:
                    send "you died"
                """);
        ticks(1);
        dispatcher.onSignal(new GameSignals.Signal("entity death", Map.of("entity", entity("minecraft:zombie"))));
        dispatcher.onSignal(new GameSignals.Signal("entity death", Map.of("entity", entity("minecraft:wither"))));
        dispatcher.onSignal(new GameSignals.Signal("entity spawn", Map.of("entity", entity("minecraft:creeper"))));
        dispatcher.onSignal(new GameSignals.Signal("entity spawn", Map.of("entity", entity("minecraft:pig"))));
        assertEquals(List.of("zombie died", "minecraft:zombie died", "boss died", "minecraft:wither died",
                "creeper", "spawned minecraft:creeper", "spawned minecraft:pig"), game.messages);
    }

    @Test
    void effectGainOfAPotionEffectType() {
        load("""
                on effect gain of speed:
                    send "fast"
                on effect gain:
                    send "got %event-effect%"
                    if event-effect is night vision:
                        send "can see"
                    if event-effect is "night_vision":
                        send "still text"
                """);
        ticks(1);
        game.effects.put("minecraft:speed", 1);
        ticks(1);
        game.effects.put("minecraft:night_vision", 1);
        ticks(1);
        assertEquals(List.of("fast", "got speed", "got night vision", "can see", "still text"), game.messages);
    }

    @Test
    void sneakAndSprintToggleFireOnBothEdges() {
        load("""
                on sneak toggle:
                    if player is sneaking:
                        send "sneak true"
                    else:
                        send "sneak false"
                on toggle sprint:
                    send "sprint"
                on sneak:
                    send "down"
                """);
        ticks(1);
        game.sneaking = true;
        ticks(1);
        game.sneaking = false;
        ticks(1);
        game.sprinting = true;
        ticks(1);
        game.sprinting = false;
        ticks(1);
        assertEquals(List.of("down", "sneak true", "sneak false", "sprint", "sprint"), game.messages);
    }

    @Test
    void consumeOfAnItem() {
        load("on eat of golden apple:\n    send \"golden\"\non consume:\n    send \"ate %event-item%\"\n");
        ticks(1);
        eat(new ItemValue("minecraft:bread", "bread", 1, 0, 0));
        eat(new ItemValue("minecraft:golden_apple", "golden apple", 1, 0, 0));
        assertEquals(List.of("ate bread", "golden", "ate golden apple"), game.messages);
    }

    private void eat(ItemValue food) {
        game.usingItem = true;
        game.consumingItem = true;
        game.useItem = food;
        game.useItemRemaining = 32;
        ticks(1);
        game.useItemRemaining = 1;
        ticks(1);
        game.usingItem = false;
        game.consumingItem = false;
        game.useItem = ItemValue.empty();
        game.useItemRemaining = 0;
        ticks(1);
    }

    @Test
    void filtersMustBeFixedValues() {
        for (String header : List.of("on break of {_block}", "on break of held item", "on death of {x}",
                "on gamemode change to {mode}")) {
            List<String> errors = load(header + ":\n    stop\n");
            assertEquals(1, errors.size(), header);
            assertTrue(errors.get(0).contains("fixed values"), errors.get(0));
        }
    }

    @Test
    void unknownFilterValuesAreParseErrors() {
        assertEquals(1, load("on gamemode change to flying:\n    stop\n").size());
        assertEquals(1, load("on weather change to snow:\n    stop\n").size());
        assertEquals(1, load("on effect gain of teleport:\n    stop\n").size());
        assertEquals(1, load("on death of zombi:\n    stop\n").size());
        assertEquals(List.of(), load("on death of mymod:frost_golem:\n    stop\n"));
    }
}
