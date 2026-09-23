package com.mineskript.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FakeGameBridgeTest {
    private final FakeGameBridge game = new FakeGameBridge();

    @Test
    void inventoryDefaultsToEmptyAndIsSettable() {
        assertEquals(36, game.inventorySize());
        assertTrue(game.heldItem().isEmpty());
        assertTrue(game.itemInSlot(5).isEmpty());
        assertEquals(36, game.freeSlots());
        game.setSlot(0, new ItemValue("minecraft:stone", "stone", 32, 0, 0));
        game.setSlot(7, new ItemValue("minecraft:stone", "stone", 8, 0, 0));
        assertEquals(new ItemValue("minecraft:stone", "stone", 32, 0, 0), game.heldItem());
        assertEquals(34, game.freeSlots());
        assertEquals(40, game.countItem("minecraft:stone"));
        assertEquals(0, game.countItem("minecraft:dirt"));
    }

    @Test
    void selectedSlotFollowsSelectSlot() {
        game.setSlot(3, new ItemValue("minecraft:dirt", "dirt", 1, 0, 0));
        game.selectSlot(3);
        assertEquals(3, game.selectedSlot());
        assertEquals("minecraft:dirt", game.heldItem().id());
        assertEquals(List.of("selectSlot:3"), game.calls);
    }

    @Test
    void effectsRecordTheirCalls() {
        game.swapHands();
        game.dropItem(false);
        game.dropItem(true);
        game.lookAt(1.0, 2.0, 3.0);
        game.setYaw(90.0);
        game.setPitch(-10.0);
        game.showTitle("t");
        game.showSubtitle("s");
        game.showActionBar("a");
        game.playSound("minecraft:ui.button.click");
        game.screenOpen = true;
        game.closeScreen();
        assertEquals(List.of("swapHands", "dropItem", "dropStack", "lookAt:1.0,2.0,3.0", "yaw:90.0", "pitch:-10.0",
                "title:t", "subtitle:s", "actionBar:a", "sound:minecraft:ui.button.click", "closeScreen"), game.calls);
    }

    @Test
    void closeScreenOnlyActsWhenAScreenIsOpen() {
        game.closeScreen();
        assertEquals(List.of(), game.calls);
        assertFalse(game.screenOpen);
        game.screenOpen = true;
        game.closeScreen();
        assertEquals(List.of("closeScreen"), game.calls);
        assertFalse(game.screenOpen);
    }

    @Test
    void entityReadingsDefaultToNothing() {
        assertNull(game.targetEntity());
        assertNull(game.nearestEntity());
        assertNull(game.nearestPlayer());
        assertEquals("minecraft:air", game.targetBlock());
        game.nearestEntity = new EntityValue("minecraft:zombie", "Zombie", 1, 2, 3, 4.5);
        assertEquals("Zombie", game.nearestEntity().name());
    }

    @Test
    void snapshotReflectsCurrentState() {
        game.health = 12;
        game.hunger = 7;
        game.sneaking = true;
        game.setSlot(0, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        WorldSnapshot snapshot = game.snapshot(true);
        assertEquals(12.0, snapshot.health());
        assertEquals(7, snapshot.hunger());
        assertTrue(snapshot.sneaking());
        assertTrue(snapshot.hasWorld());
        assertEquals(41, snapshot.inventory().size());
        assertEquals("minecraft:stone", snapshot.inventory().get(0).id());
        game.hasWorld = false;
        assertEquals(WorldSnapshot.empty(), game.snapshot(true));
    }

    @Test
    void newReadingsHaveDefaultsAndAreSettable() {
        assertEquals("minecraft:plains", game.biome());
        assertEquals(15, game.lightLevel());
        assertEquals(15, game.skyLight());
        assertTrue(game.canSeeSky());
        assertEquals("vanilla", game.serverBrand());
        assertEquals(42, game.ping());
        assertEquals(120, game.fps());
        assertEquals(0, game.totalExperience());
        assertNull(game.vehicle());
        game.biome = "minecraft:desert";
        game.vehicle = new EntityValue("minecraft:horse", "Horse", 1, 2, 3, 0.5);
        assertEquals("minecraft:desert", game.biome());
        assertEquals("Horse", game.vehicle().name());
    }

    @Test
    void effectLevelNormalisesLooseNames() {
        game.effects.put("minecraft:speed", 2);
        game.effects.put("minecraft:fire_resistance", 1);
        assertEquals(2, game.effectLevel("speed"));
        assertEquals(2, game.effectLevel("Speed"));
        assertEquals(2, game.effectLevel("minecraft:speed"));
        assertEquals(1, game.effectLevel("fire resistance"));
        assertEquals(0, game.effectLevel("jump boost"));
        assertEquals(Map.of("minecraft:speed", 2, "minecraft:fire_resistance", 1), game.activeEffects());
    }

    @Test
    void onlineNamesComeBackSorted() {
        game.onlineNames.add("Zoe");
        game.onlineNames.add("Alex");
        game.onlineNames.add("Mel");
        assertEquals(List.of("Alex", "Mel", "Zoe"), game.onlinePlayerNames());
    }

    @Test
    void blockAtUsesAbsoluteCoordinates() {
        game.setBlockAt(10, 64, -3, "minecraft:stone");
        assertEquals("minecraft:stone", game.blockAt(10.9, 64.2, -3.0));
        assertEquals("minecraft:air", game.blockAt(11, 64, -3));
    }

    @Test
    void newActionsRecordTheirCalls() {
        game.openInventory();
        game.copyToClipboard("hello");
        game.takeScreenshot();
        game.disconnect();
        assertEquals(List.of("openInventory", "clipboard:hello", "screenshot", "disconnect"), game.calls);
    }

    @Test
    void theSnapshotCarriesTheNewComponents() {
        game.setSlot(0, new ItemValue("minecraft:bread", "bread", 3, 0, 0));
        game.useItem = new ItemValue("minecraft:bread", "bread", 3, 0, 0);
        game.consumingItem = true;
        game.useItemRemaining = 12;
        game.totalExperience = 350;
        game.dimension = "minecraft:the_nether";
        game.onlineNames.add("Alex");
        WorldSnapshot snapshot = game.snapshot(false);
        assertEquals("minecraft:bread", snapshot.heldItem().id());
        assertEquals("minecraft:bread", snapshot.useItem().id());
        assertTrue(snapshot.consumingItem());
        assertEquals(12, snapshot.useItemRemaining());
        assertEquals(350, snapshot.totalExperience());
        assertEquals("minecraft:the_nether", snapshot.dimension());
        assertEquals(List.of("Alex"), snapshot.onlineNames());
    }
}
