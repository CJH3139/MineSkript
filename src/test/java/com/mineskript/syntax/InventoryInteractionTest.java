package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class InventoryInteractionTest {
    private final ScriptRunner runner = new ScriptRunner();

    private static ItemValue item(String id, int count) {
        return new ItemValue("minecraft:" + id, id.replace('_', ' '), count, 0, 0);
    }

    private void chest(ItemValue... items) {
        runner.game.chestOpen = true;
        runner.game.inventoryScreen = true;
        runner.game.container.clear();
        runner.game.container.addAll(List.of(items));
    }

    @Test
    void everyClickFormReachesTheGame() {
        chest(ItemValue.empty(), ItemValue.empty(), ItemValue.empty());
        runner.run("""
                on load:
                    click slot 1
                    left click on slot 2 of player's inventory
                    right click slot 3
                    shift click slot 4
                    shift left click slot 5
                    shift right click slot 6
                    middle click slot 7
                    double click slot 8
                    click slot 2 of the current inventory
                    shift click slot 0 in top inventory
                """);
        assertEquals(List.of("click:LOCAL:1:PICKUP:0", "click:LOCAL:2:PICKUP:0", "click:LOCAL:3:PICKUP:1",
                "click:LOCAL:4:QUICK_MOVE:0", "click:LOCAL:5:QUICK_MOVE:0", "click:LOCAL:6:QUICK_MOVE:1",
                "click:LOCAL:7:CLONE:2", "click:LOCAL:8:PICKUP_ALL:0", "click:OPEN:2:PICKUP:0",
                "click:OPEN:0:QUICK_MOVE:0"), runner.game.calls);
    }

    @Test
    void slotsTheScreenDoesNotShowAreLeftAlone() {
        chest(ItemValue.empty());
        runner.run("on load:\n    click slot 38\n    click slot 5 of current inventory\n    click slot -1\n");
        assertEquals(List.of(), runner.game.calls);
    }

    @Test
    void droppingAndSwappingSlots() {
        runner.game.setSlot(3, item("dirt", 5));
        runner.run("""
                on load:
                    drop slot 3
                    drop an item from slot 3
                    drop the stack in slot 3
                    swap slot 9 with hotbar slot 2
                    swap slot 9 with the offhand
                    swap slot 9 with hotbar slot 9
                """);
        assertEquals(List.of("click:LOCAL:3:THROW:0", "click:LOCAL:3:THROW:0", "click:LOCAL:3:THROW:1",
                "click:LOCAL:9:SWAP:2", "click:LOCAL:9:SWAP:40"), runner.game.calls);
        assertTrue(runner.game.itemInSlot(3).isEmpty());
    }

    @Test
    void movingASlotPicksUpAndPutsDown() {
        runner.game.setSlot(9, item("stone", 10));
        runner.game.setSlot(0, item("dirt", 4));
        runner.run("on load:\n    move the stack in slot 9 to slot 0\n");
        assertEquals("minecraft:stone", runner.game.itemInSlot(0).id());
        assertEquals("minecraft:dirt", runner.game.itemInSlot(9).id());
        assertTrue(runner.game.cursorItem().isEmpty());
        assertEquals(List.of("click:LOCAL:9:PICKUP:0", "click:LOCAL:0:PICKUP:0", "click:LOCAL:9:PICKUP:0"),
                runner.game.calls);
    }

    @Test
    void movingIntoAnEmptySlotTakesTwoClicks() {
        chest(item("diamond", 3), ItemValue.empty());
        runner.run("on load:\n    move item from slot 0 of current inventory to slot 35 of player's inventory\n");
        assertEquals(3, runner.game.itemInSlot(35).count());
        assertTrue(runner.game.openInventoryItems().get(0).isEmpty());
        assertEquals(List.of("click:OPEN:0:PICKUP:0", "click:LOCAL:35:PICKUP:0"), runner.game.calls);
    }

    @Test
    void quickMoveShiftClicksEveryMatchingStack() {
        chest(item("diamond", 3), item("dirt", 1), item("diamond", 2), ItemValue.empty());
        runner.run("on load:\n    quick move all diamonds in the current inventory\n");
        assertEquals(List.of("click:OPEN:0:QUICK_MOVE:0", "click:OPEN:2:QUICK_MOVE:0"), runner.game.calls);
        assertEquals("minecraft:dirt", runner.game.openInventoryItems().get(1).id());
    }

    @Test
    void quickMoveEverythingSkipsArmourAndEmptySlots() {
        chest(ItemValue.empty(), ItemValue.empty());
        runner.game.setSlot(0, item("stone", 1));
        runner.game.setSlot(37, item("iron_leggings", 1));
        runner.run("on load:\n    shift click everything in player's inventory\n");
        assertEquals(List.of("click:LOCAL:0:QUICK_MOVE:0"), runner.game.calls);
    }

    @Test
    void theCurrentInventoryIsReadLikeYourOwn() {
        chest(item("diamond", 5), ItemValue.empty(), item("diamond", 2), item("stone", 64));
        runner.run("""
                on load:
                    send "%slot 0 of current inventory%"
                    send "%current inventory's slot 3%"
                    send "%slot 9 of current inventory%"
                    send "%amount of diamond in current inventory%"
                    send "%amount of slots of current inventory%"
                    send "%rows of the current inventory%"
                    send "%rows of player's inventory%"
                    send "%player's inventory's amount of slots%"
                    send "%size of items in current inventory%"
                    send "%first empty slot of current inventory%"
                    send "%first empty slot in player's inventory%"
                    if current inventory has 7 diamonds:
                        send "has"
                    if player doesn't have diamond:
                        send "own inventory is separate"
                    if current inventory is not full:
                        send "not full"
                    send "%current inventory of player%"
                """);
        assertEquals(List.of("5 diamond", "64 stone", "air", "7", "4", "0", "4", "41", "3", "1", "0", "has",
                "own inventory is separate", "not full", "current inventory of Steve"), runner.game.messages);
    }

    @Test
    void aFullInventoryHasNoFirstEmptySlot() {
        chest(item("stone", 1));
        runner.run("""
                on load:
                    if first empty slot of current inventory is not set:
                        send "full"
                    if current inventory is full:
                        send "is full"
                """);
        assertEquals(List.of("full", "is full"), runner.game.messages);
    }

    @Test
    void canHoldCountsEmptySlotsAndRoomInStacks() {
        for (int slot = 0; slot < 36; slot++) {
            runner.game.setSlot(slot, item("dirt", 64));
        }
        runner.game.setSlot(5, item("cobblestone", 60));
        runner.game.setSlot(6, ItemValue.empty());
        runner.game.itemIds.add("minecraft:cobblestone");
        runner.game.itemIds.add("minecraft:ender_pearl");
        runner.run("""
                on load:
                    if player can hold 68 cobblestone:
                        send "68 fits"
                    if player can't hold 69 cobblestone:
                        send "69 does not"
                    if player's inventory has enough space for 16 ender pearls:
                        send "16 pearls fit"
                    if player cannot hold 17 ender pearls:
                        send "17 pearls do not"
                    if player has space for cobblestone:
                        send "one fits"
                    if player doesn't have enough space to hold 100 of cobblestone or ender pearl:
                        send "neither fits 100"
                """);
        assertEquals(List.of("68 fits", "69 does not", "16 pearls fit", "17 pearls do not", "one fits",
                "neither fits 100"), runner.game.messages);
    }

    @Test
    void theCursorSlotIsWhatYouCarry() {
        runner.game.cursor = item("apple", 2);
        runner.run("on load:\n    send \"%cursor slot of player%\"\n    send \"%player's cursor slot%\"\n    send \"%the cursor item%\"\n");
        assertEquals(List.of("2 apple", "2 apple", "2 apple"), runner.game.messages);
    }

    @Test
    void loopingTheItemsOfAnInventory() {
        runner.game.setSlot(0, item("stone", 1));
        runner.game.setSlot(40, item("shield", 1));
        runner.run("on load:\n    loop all items in player's inventory:\n        send \"%loop-value%\"\n");
        assertEquals(List.of("stone", "shield"), runner.game.messages);
    }

    @Test
    void clickedValuesOnlyWorkInInventoryClick() {
        assertEquals(List.of("t.sk:2: clicked slot only works in on inventory click"),
                runner.errorsOf("on load:\n    send \"%clicked slot%\"\n"));
        assertEquals(List.of("t.sk:2: click (type|action) only works in on inventory click"),
                runner.errorsOf("on chat send:\n    send click type\n"));
        assertEquals(List.of(), runner.errorsOf("""
                on inventory click:
                    send "%clicked slot% %index of clicked slot% %clicked item% %click type% %click action%"
                    if clicked inventory is the current inventory:
                        cancel event
                """));
    }

    @Test
    void theNewFormsNeedAWorld() {
        runner.game.hasWorld = false;
        runner.run("on load:\n    click slot 1\n");
        assertEquals(List.of("t.sk:2: no world"), runner.game.errors);
        assertFalse(runner.game.calls.contains("click:LOCAL:1:PICKUP:0"));
    }

    @Test
    void closeTheInventoryOfPlayerIsSkriptsForm() {
        runner.game.screenOpen = true;
        runner.run("on load:\n    close the inventory of player\n");
        assertEquals(List.of("closeScreen"), runner.game.calls);
    }
}
