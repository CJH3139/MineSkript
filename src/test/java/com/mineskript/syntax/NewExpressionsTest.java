package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewExpressionsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void itemsAndInventory() {
        runner.game.setSlot(0, new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 1, 12, 1561));
        runner.game.setSlot(5, new ItemValue("minecraft:stone", "stone", 30, 0, 0));
        runner.game.offhand = new ItemValue("minecraft:torch", "torch", 7, 0, 0);
        runner.run("""
                on load:
                    send "%held item%"
                    send "%offhand item%"
                    send "%item in slot 5%"
                    send "%name of held item% %id of held item% %count of held item%"
                    send "%damage of held item% of %max damage of held item%"
                    send "%number of stone in inventory%"
                    send "%selected slot% %free slots% %used slots%"
                """);
        assertEquals(List.of("diamond pickaxe", "7 torch", "30 stone",
                "diamond pickaxe minecraft:diamond_pickaxe 1", "12 of 1561", "30", "0 34 2"), runner.game.messages);
    }

    @Test
    void anEmptyHandReadsAsAirNotNone() {
        runner.run("on load:\n    send \"%held item%\"\n    if held item is air:\n        send \"empty\"\n");
        assertEquals(List.of("air", "empty"), runner.game.messages);
    }

    @Test
    void anItemComparesWithABlockTypeById() {
        runner.game.setSlot(0, new ItemValue("minecraft:stone", "stone", 5, 0, 0));
        runner.run("on load:\n    if held item is stone:\n        send \"holding stone\"\n    if held item is dirt:\n        send \"never\"\n");
        assertEquals(List.of("holding stone"), runner.game.messages);
    }

    @Test
    void playerStateAndWorld() {
        runner.game.gamemode = "creative";
        runner.game.xpLevel = 30;
        runner.game.air = 250;
        runner.game.armor = 11;
        runner.game.yaw = 90;
        runner.game.pitch = -12.5;
        runner.game.dimension = "minecraft:the_nether";
        runner.game.gameTime = 4321;
        runner.game.playersOnline = 3;
        runner.game.difficulty = "hard";
        runner.run("""
                on load:
                    send "%gamemode% %xp level% %air% of %max air% %armor%"
                    send "%yaw% %pitch% %dimension%"
                    send "%game time% %players online% %difficulty%"
                """);
        assertEquals(List.of("creative 30 250 of 300 11", "90 -12.5 minecraft:the_nether", "4321 3 hard"), runner.game.messages);
    }

    @Test
    void targetingAndNearestEntities() {
        runner.game.targetBlock = "minecraft:diamond_ore";
        runner.game.nearestEntity = new EntityValue("minecraft:zombie", "Zombie", 10.0, 64.0, 2.0, 7.5);
        runner.run("""
                on load:
                    send "%target block%"
                    send "%nearest entity%"
                    send "%name of nearest entity% %id of nearest entity%"
                    send "%distance of nearest entity%"
                    send "%x coordinate of nearest entity%"
                """);
        assertEquals(List.of("diamond_ore", "Zombie", "Zombie minecraft:zombie", "7.5", "10"), runner.game.messages);
    }

    @Test
    void anAbsentEntityReadsAsNoneAndItsPropertiesError() {
        runner.run("on load:\n    send \"%nearest entity%\"\n    send \"%target entity%\"\n");
        assertEquals(List.of("<none>", "<none>"), runner.game.messages);
        runner.game.messages.clear();
        runner.run("on load:\n    send \"%name of nearest entity%\"\n");
        assertEquals(List.of("t.ms:2: there is no entity"), runner.game.errors);
    }

    @Test
    void expressionsWorkInsideConditionsLoopsAndVariables() {
        runner.game.setSlot(0, new ItemValue("minecraft:stone", "stone", 5, 0, 0));
        runner.game.xpLevel = 4;
        runner.run("""
                on load:
                    set {tool} to held item
                    send "%{tool}%"
                    if count of held item is greater than 3:
                        send "plenty"
                    loop 2 times:
                        send "%xp level% %loop-value%"
                    set {total} to count of held item + xp level
                    send "%{total}%"
                """);
        assertEquals(List.of("5 stone", "plenty", "4 1", "4 2", "9"), runner.game.messages);
    }
}
