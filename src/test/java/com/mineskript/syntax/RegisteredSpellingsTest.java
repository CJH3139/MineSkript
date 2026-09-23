package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.script.EventDispatcher;
import com.mineskript.script.ScriptRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegisteredSpellingsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void heldAndOffhandItemSpellings() {
        runner.game.setSlot(0, new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 1, 12, 1561));
        runner.game.offhand = new ItemValue("minecraft:torch", "torch", 7, 0, 0);
        runner.run("""
                on load:
                    send "%item in hand%"
                    send "%tool%"
                    send "%item in offhand%"
                """);
        assertEquals(List.of("diamond pickaxe", "diamond pickaxe", "7 torch"), runner.game.messages);
    }

    @Test
    void playerStateSpellings() {
        runner.game.xpLevel = 7;
        runner.game.xpProgress = 0.5;
        runner.game.speed = 0.25;
        runner.game.fallDistance = 2.5;
        runner.game.nearestPlayer = new EntityValue("minecraft:player", "Alex", 1, 2, 3, 6.0);
        runner.run("""
                on load:
                    send "%experience level%"
                    send "%xp progress%"
                    send "%speed%"
                    send "%fall distance%"
                    send "%nearest player%"
                """);
        assertEquals(List.of("7", "0.5", "0.25", "2.5", "Alex"), runner.game.messages);
    }

    @Test
    void itemPossessiveAndAmountSpellings() {
        runner.game.setSlot(0, new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 3, 12, 1561));
        runner.run("""
                on load:
                    send "%amount of held item%"
                    send "%held item's amount%"
                    send "%held item's name%"
                    send "%held item's id%"
                    send "%held item's count%"
                    send "%held item's damage%"
                    send "%held item's max damage%"
                """);
        assertEquals(List.of("3", "3", "diamond pickaxe", "minecraft:diamond_pickaxe", "3", "12", "1561"),
                runner.game.messages);
    }

    @Test
    void entityPossessiveSpellings() {
        runner.game.nearestEntity = new EntityValue("minecraft:zombie", "Zombie", 10.5, 64.0, -2.0, 7.5);
        runner.run("""
                on load:
                    send "%nearest entity's name%"
                    send "%nearest entity's id%"
                    send "%nearest entity's distance%"
                    send "%nearest entity's x-coordinate%"
                    send "%nearest entity's y coordinate%"
                    send "%nearest entity's z coord%"
                """);
        assertEquals(List.of("Zombie", "minecraft:zombie", "7.5", "10.5", "64", "-2"), runner.game.messages);
    }

    @Test
    void playerCoordinateSpellings() {
        runner.run("""
                on load:
                    send "%player's y-coordinate%"
                    send "%y coordinate of player%"
                    send "%player's z-coord%"
                    send "%z-coordinate of player%"
                    send "%player's x coord%"
                """);
        assertEquals(List.of("64", "64", "-3.5", "-3.5", "0.5"), runner.game.messages);
    }

    @Test
    void negatedAndWeatherConditionSpellings() {
        runner.game.setSlot(0, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        runner.game.setSlot(2, new ItemValue("minecraft:dirt", "dirt", 1, 0, 0));
        runner.game.thundering = true;
        runner.run("""
                on load:
                    if player isn't holding diamond:
                        send "not diamond"
                    if player isn't holding stone:
                        send "never"
                    if slot 2 is not empty:
                        send "slot 2 filled"
                    if slot 3 is not empty:
                        send "never either"
                    if gamemode is not "creative":
                        send "not creative"
                    if it is not raining:
                        send "dry"
                    if it is thundering:
                        send "storm"
                """);
        assertEquals(List.of("not diamond", "slot 2 filled", "not creative", "dry", "storm"), runner.game.messages);
    }

    @Test
    void stopSprintingAndHungerChangeFire() {
        FakeGameBridge game = new FakeGameBridge();
        ScriptRegistry registry = new ScriptRegistry();
        EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler());
        registry.replace(List.of(new Parser(DefaultSyntax.registry()).parse("t.ms", """
                on stop sprinting:
                    send "stopped sprinting"
                on hunger change:
                    send "hunger %event-hunger change%"
                """)));
        game.sprinting = true;
        dispatcher.tick();
        game.sprinting = false;
        game.hunger = 17;
        dispatcher.tick();
        assertEquals(List.of("stopped sprinting", "hunger -3"), game.messages);
    }
}
