package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.CombatHit;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class CombatEventValuesTest {
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

    @Test
    void damageGivesTheCauseAndAttackerTheServerReported() {
        load("""
                on damage:
                    if damage cause is fall:
                        send "fell"
                    if event-damage cause is entity attack:
                        send "hit by %attacker%"
                    if attacker is not set:
                        send "no attacker"
                """);
        dispatcher.tick();
        game.hit = new CombatHit("minecraft:fall", null);
        game.health = 17;
        dispatcher.tick();
        game.hit = new CombatHit("minecraft:mob_attack", new EntityValue("minecraft:zombie", "Zombie", 0, 64, 0, 2));
        game.health = 14;
        dispatcher.tick();
        assertEquals(List.of("fell", "no attacker", "hit by Zombie"), game.messages);
    }

    @Test
    void damageWithoutAReportedHitHasNoCause() {
        load("""
                on damage:
                    if damage cause is set:
                        send "cause"
                    else:
                        send "unknown"
                """);
        dispatcher.tick();
        game.health = 15;
        dispatcher.tick();
        assertEquals(List.of("unknown"), game.messages);
    }

    @Test
    void deathGivesTheDeathMessage() {
        load("""
                on death:
                    send "%death message%"
                    send "%event-death message%"
                """);
        dispatcher.tick();
        game.deathMessage = "Alex fell from a high place";
        game.health = 0;
        dispatcher.tick();
        assertEquals(List.of("Alex fell from a high place", "Alex fell from a high place"), game.messages);
    }

    @Test
    void effectGainGivesTheDuration() {
        load("""
                on effect gain:
                    if event-effect duration is set:
                        send "%event-effect% for %event-effect duration%"
                    else:
                        send "%event-effect% forever"
                """);
        dispatcher.tick();
        game.effects.put("minecraft:speed", 1);
        game.effectDurations.put("minecraft:speed", 600);
        dispatcher.tick();
        game.effects.put("minecraft:night_vision", 1);
        game.effectDurations.put("minecraft:night_vision", -1);
        dispatcher.tick();
        assertEquals(List.of("speed for 600 ticks", "night vision forever"), game.messages);
    }

    @Test
    void closingScreensGiveTheTitleOfTheScreenThatClosed() {
        load("""
                on screen close:
                    send "closed %event-screen title%"
                on inventory close:
                    send "inventory %event-screen title%"
                """);
        dispatcher.tick();
        game.screenOpen = true;
        game.inventoryScreen = true;
        game.screenTitle = "Chest";
        dispatcher.tick();
        game.screenOpen = false;
        game.inventoryScreen = false;
        dispatcher.tick();
        assertEquals(List.of("closed Chest", "inventory Chest"), game.messages);
    }
}
