package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PolledEventsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
    });

    private void load(String source) {
        registry.replace(List.of(new Parser(DefaultSyntax.registry()).parse("t.ms", source)));
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void theFirstTickIsABaselineAndFiresOnlyWorldJoin() {
        load("on move:\n    send \"moved\"\non world join:\n    send \"joined\"\n");
        game.x = 10;
        ticks(1);
        assertEquals(List.of("joined"), game.messages);
        ticks(1);
        assertEquals(List.of("joined"), game.messages);
    }

    @Test
    void moveFiresOnBlockChangeWithFromAndTo() {
        load("on move:\n    send \"%event-from x% to %event-to x%\"\n");
        ticks(1);
        game.x = 5.5;
        ticks(1);
        assertEquals(List.of("0 to 5"), game.messages);
        game.x = 5.9;
        ticks(1);
        assertEquals(List.of("0 to 5"), game.messages);
    }

    @Test
    void damageHealAndDeath() {
        load("on damage:\n    send \"hurt %event-damage%\"\non heal:\n    send \"healed %event-healed%\"\non death:\n    send \"died\"\non respawn:\n    send \"back\"\n");
        ticks(1);
        game.health = 14;
        ticks(1);
        game.health = 18;
        ticks(1);
        game.health = 0;
        ticks(1);
        game.health = 20;
        ticks(1);
        assertEquals(List.of("hurt 6", "healed 4", "died", "back"), game.messages);
    }

    @Test
    void sneakSprintJumpAndLand() {
        load("""
                on sneak:
                    send "sneak"
                on stop sneaking:
                    send "unsneak"
                on sprint:
                    send "sprint"
                on jump:
                    send "jump"
                on land:
                    send "land %event-fall distance%"
                """);
        ticks(1);
        game.sneaking = true;
        ticks(1);
        game.sneaking = false;
        ticks(1);
        game.sprinting = true;
        ticks(1);
        game.onGround = false;
        game.velocityY = 0.42;
        ticks(1);
        game.fallDistance = 3;
        game.onGround = true;
        ticks(1);
        assertEquals(List.of("sneak", "unsneak", "sprint", "jump", "land 3"), game.messages);
    }

    @Test
    void heldItemChangeExposesBothItems() {
        load("on held item change:\n    send \"%event-previous item% to %event-item%\"\n");
        game.setSlot(0, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        game.setSlot(1, new ItemValue("minecraft:dirt", "dirt", 1, 0, 0));
        ticks(1);
        game.selected = 1;
        ticks(1);
        assertEquals(List.of("stone to dirt"), game.messages);
    }

    @Test
    void inventoryChangeFiresOnCountOrIdChange() {
        load("on inventory change:\n    send \"changed to %event-item%\"\n");
        game.setSlot(4, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        ticks(1);
        game.setSlot(4, new ItemValue("minecraft:stone", "stone", 2, 0, 0));
        ticks(1);
        game.setSlot(4, new ItemValue("minecraft:dirt", "dirt", 2, 0, 0));
        ticks(1);
        ticks(3);
        assertEquals(List.of("changed to 2 stone", "changed to 2 dirt"), game.messages);
    }

    @Test
    void inventoryChangeIgnoresDurabilityAndFollowsCount() {
        load("on inventory change:\n    send \"changed\"\n");
        game.setSlot(0, new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 1, 0, 1561));
        ticks(1);
        game.setSlot(0, new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 1, 1, 1561));
        ticks(1);
        assertEquals(List.of(), game.messages);
        game.setSlot(0, new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 2, 1, 1561));
        ticks(1);
        assertEquals(List.of("changed"), game.messages);
    }

    @Test
    void theInventoryIsOnlyWalkedWhenALoadedTriggerNeedsIt() {
        load("on move:\n    send \"moved\"\n");
        ticks(2);
        assertEquals(0, game.inventoryScans);
        load("on inventory change:\n    send \"changed\"\n");
        ticks(1);
        assertEquals(1, game.inventoryScans);
        assertEquals(List.of(), game.messages);
        game.setSlot(4, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        ticks(1);
        assertEquals(List.of("changed"), game.messages);
    }

    @Test
    void usingItemLevelGamemodeWeatherAndScreen() {
        load("""
                on start using item:
                    send "start"
                on stop using item:
                    send "stop"
                on level change:
                    send "level change %event-level change%"
                on gamemode change:
                    send "mode %event-gamemode%"
                on weather change:
                    send "weather"
                on screen open:
                    send "open"
                on screen close:
                    send "close"
                """);
        game.xpLevel = 29;
        ticks(1);
        game.usingItem = true;
        ticks(1);
        game.usingItem = false;
        ticks(1);
        game.xpLevel = 30;
        ticks(1);
        game.gamemode = "creative";
        ticks(1);
        game.raining = true;
        ticks(1);
        game.screenOpen = true;
        ticks(1);
        game.screenOpen = false;
        ticks(1);
        assertEquals(List.of("start", "stop", "level change 1", "mode creative", "weather", "open", "close"), game.messages);
    }

    @Test
    void worldLeaveFiresAndResetsTheBaseline() {
        load("on world join:\n    send \"join\"\non world leave:\n    send \"leave\"\non move:\n    send \"move\"\n");
        ticks(1);
        game.hasWorld = false;
        ticks(1);
        game.hasWorld = true;
        game.x = 40;
        ticks(1);
        assertEquals(List.of("join", "leave", "join"), game.messages);
    }

    @Test
    void anEventValueOutsideItsEventIsAParseError() {
        List<String> errors = new Parser(DefaultSyntax.registry()).parse("t.ms", "on load:\n    send \"%event-damage%\"\n")
                .errors().stream().map(Object::toString).toList();
        assertEquals(List.of("t.ms:2: event-damage is not available in this event"), errors);
        List<String> wrongEvent = new Parser(DefaultSyntax.registry()).parse("t.ms", "on heal:\n    send \"%event-damage%\"\n")
                .errors().stream().map(Object::toString).toList();
        assertTrue(wrongEvent.get(0).endsWith("event-damage is not available in this event"));
    }

    @Test
    void aWaitInsideAPolledEventResumesOnTheNextTickNotTheSameOne() {
        load("on move:\n    send \"a\"\n    wait 1 tick\n    send \"b\"\n");
        ticks(1);
        game.x = 10;
        ticks(1);
        assertEquals(List.of("a"), game.messages);
        ticks(1);
        assertEquals(List.of("a", "b"), game.messages);
    }

    @Test
    void worldJoinRunsTheJoinHook() {
        List<String> flushed = new ArrayList<>();
        EventDispatcher withHook = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
        }, () -> flushed.add("flushed"));
        withHook.tick();
        assertEquals(List.of("flushed"), flushed);
    }
}
