package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewConditionsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void playerFlags() {
        runner.game.inWater = true;
        runner.game.onFire = true;
        runner.run("""
                on load:
                    if player is in water:
                        send "wet"
                    if player is not in lava:
                        send "not lava"
                    if player is on fire:
                        send "burning"
                    if player is flying:
                        send "never"
                    if player is not sleeping:
                        send "awake"
                """);
        assertEquals(List.of("wet", "not lava", "burning", "awake"), runner.game.messages);
    }

    @Test
    void itemsAndInventory() {
        runner.game.setSlot(0, new ItemValue("minecraft:diamond", "diamond", 3, 0, 0));
        runner.run("""
                on load:
                    if player has diamond:
                        send "has"
                    if player has emerald:
                        send "never"
                    if player does not have emerald:
                        send "no emerald"
                    if player is holding diamond:
                        send "holding"
                    if slot 1 is empty:
                        send "slot 1 empty"
                    if inventory is empty:
                        send "never either"
                    if inventory is not full:
                        send "room left"
                """);
        assertEquals(List.of("has", "no emerald", "holding", "slot 1 empty", "room left"), runner.game.messages);
    }

    @Test
    void aFullMainInventoryIsFullEvenWithArmourAndAnOffhandItem() {
        for (int slot = 0; slot < FakeGameBridge.MAIN_SLOTS; slot++) {
            runner.game.setSlot(slot, new ItemValue("minecraft:stone", "stone", 64, 0, 0));
        }
        for (int slot = FakeGameBridge.MAIN_SLOTS; slot <= FakeGameBridge.OFFHAND_SLOT; slot++) {
            runner.game.setSlot(slot, new ItemValue("minecraft:iron_helmet", "iron helmet", 1, 0, 0));
        }
        runner.run("""
                on load:
                    if inventory is full:
                        send "full"
                    if inventory is not full:
                        send "never"
                    send "%free slots% %used slots%"
                """);
        assertEquals(List.of("full", "0 36"), runner.game.messages);
    }

    @Test
    void anEntityComparesWithTextById() {
        runner.game.nearestEntity = new EntityValue("minecraft:zombie", "Zombie", 0, 0, 0, 4.0);
        runner.run("""
                on load:
                    if nearest entity is "zombie":
                        send "zombie"
                    if nearest entity is "minecraft:zombie":
                        send "full id"
                    if nearest entity is "creeper":
                        send "never"
                    if nearest entity is not "creeper":
                        send "not a creeper"
                """);
        assertEquals(List.of("zombie", "full id", "not a creeper"), runner.game.messages);
    }

    @Test
    void weatherGamemodeAndDistance() {
        runner.game.raining = true;
        runner.game.gamemode = "creative";
        runner.game.nearestEntity = new EntityValue("minecraft:zombie", "Zombie", 0, 0, 0, 4.0);
        runner.run("""
                on load:
                    if it is raining:
                        send "rain"
                    if it is not thundering:
                        send "no storm"
                    if gamemode is "creative":
                        send "creative"
                    if gamemode is "survival":
                        send "never"
                    if nearest entity is within 5 blocks:
                        send "close"
                    if nearest entity is within 2 blocks:
                        send "never either"
                """);
        assertEquals(List.of("rain", "no storm", "creative", "close"), runner.game.messages);
    }

    @Test
    void anAbsentEntityIsNeverWithinRange() {
        runner.run("on load:\n    if nearest entity is within 100 blocks:\n        send \"never\"\n    send \"done\"\n");
        assertEquals(List.of("done"), runner.game.messages);
    }

    @Test
    void anAbsentEntityNegatesNormally() {
        runner.run("""
                on load:
                    if nearest entity is within 5 blocks:
                        send "never"
                    if nearest entity is not within 5 blocks:
                        send "nothing near"
                """);
        assertEquals(List.of("nothing near"), runner.game.messages);
    }

    @Test
    void newConditionsCombineWithAndOr() {
        runner.game.inWater = true;
        runner.game.setSlot(0, new ItemValue("minecraft:diamond", "diamond", 1, 0, 0));
        runner.run("""
                on load:
                    if player is in water and player has diamond:
                        send "both"
                    if all:
                        player is in water
                        player is holding diamond
                    then:
                        send "section"
                """);
        assertEquals(List.of("both", "section"), runner.game.messages);
    }
}
