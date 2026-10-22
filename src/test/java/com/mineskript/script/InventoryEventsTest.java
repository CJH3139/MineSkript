package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.InventoryRef;
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

class InventoryEventsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000),
            new Scheduler(), new Variables(), () -> {
            });

    private void load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.sk", source);
        assertEquals(List.of(), script.errors());
        registry.replace(List.of(script));
    }

    private static Map<String, Object> click(double slot, ItemValue item, String type, InventoryRef inventory) {
        return Map.of("slot", slot, "item", item, "click type", type, "inventory", inventory);
    }

    @Test
    void aClickGivesItsValuesAndCanBeCancelled() {
        load("""
                on inventory click:
                    send "%clicked slot% %clicked item% %click type% %clicked inventory%"
                    if clicked inventory is the current inventory:
                        if clicked slot is 13:
                            cancel event
                """);
        ItemValue sword = new ItemValue("minecraft:diamond_sword", "diamond sword", 1, 0, 1561);
        assertTrue(dispatcher.onInventoryClick(click(4, sword, "left mouse button", InventoryRef.LOCAL)));
        assertFalse(dispatcher.onInventoryClick(click(13, sword, "number key", InventoryRef.OPEN)));
        assertEquals(List.of("4 diamond sword left mouse button inventory of Steve",
                "13 diamond sword number key current inventory of Steve"), game.messages);
    }

    @Test
    void aCancelAfterAWaitIsTooLate() {
        load("on inventory click:\n    wait 1 tick\n    cancel event\n");
        assertTrue(dispatcher.onInventoryClick(click(0, ItemValue.empty(), "drop key", InventoryRef.LOCAL)));
    }

    @Test
    void anItemFilterOnlyFiresForThoseItems() {
        load("on inventory click on diamond:\n    send \"diamond\"\n");
        dispatcher.onInventoryClick(click(0, new ItemValue("minecraft:dirt", "dirt", 1, 0, 0), "left mouse button",
                InventoryRef.LOCAL));
        dispatcher.onInventoryClick(click(1, new ItemValue("minecraft:diamond", "diamond", 1, 0, 0),
                "left mouse button", InventoryRef.LOCAL));
        assertEquals(List.of("diamond"), game.messages);
    }

    @Test
    void openingAndClosingAnInventoryScreen() {
        load("""
                on inventory open:
                    send "open %event-screen title%"
                on inventory close:
                    send "closed"
                on inventory opening:
                    send "opening"
                on inventory closing:
                    send "closing"
                """);
        dispatcher.tick();
        game.screenOpen = true;
        game.screenTitle = "Chest";
        game.inventoryScreen = true;
        dispatcher.tick();
        dispatcher.tick();
        game.inventoryScreen = false;
        game.screenOpen = false;
        dispatcher.tick();
        assertEquals(List.of("open Chest", "opening", "closed", "closing"), game.messages);
    }

    @Test
    void leavingTheWorldWithAChestOpenDoesNotCloseItLater() {
        load("on inventory close:\n    send \"closed\"\n");
        game.inventoryScreen = true;
        dispatcher.tick();
        game.hasWorld = false;
        game.inventoryScreen = false;
        dispatcher.tick();
        game.hasWorld = true;
        dispatcher.tick();
        assertEquals(List.of(), game.messages);
    }

    @Test
    void theDispatcherKnowsWhenNobodyListensForClicks() {
        load("on chat:\n    stop\n");
        dispatcher.tick();
        assertFalse(dispatcher.listens("inventory click"));
        load("on inventory click:\n    stop\n");
        dispatcher.tick();
        assertTrue(dispatcher.listens("inventory click"));
    }
}
