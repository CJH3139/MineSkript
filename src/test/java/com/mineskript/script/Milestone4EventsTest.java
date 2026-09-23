package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

class Milestone4EventsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
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

    @Test
    void effectGainAndLoseCarryTheIdAndTheLevel() {
        load("on effect gain:\n    send \"gained %event-effect% %event-effect level%\"\non effect lose:\n    send \"lost %event-effect%\"\n");
        game.effects.put("minecraft:speed", 1);
        ticks(1);
        game.effects.put("minecraft:haste", 2);
        ticks(1);
        game.effects.remove("minecraft:speed");
        ticks(1);
        assertEquals(List.of("gained haste 2", "lost speed"), game.messages);
    }

    @Test
    void aLevelChangeOnAnExistingEffectFiresNothing() {
        assertEquals(List.of(), load("on effect gain:\n    send \"gain\"\non effect lose:\n    send \"lose\"\n"));
        game.effects.put("minecraft:speed", 1);
        ticks(1);
        game.effects.put("minecraft:speed", 2);
        ticks(1);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void severalEffectsInOneTickFireAlphabetically() {
        load("on effect gain:\n    send \"%event-effect%\"\n");
        ticks(1);
        game.effects.put("minecraft:speed", 1);
        game.effects.put("minecraft:haste", 1);
        game.effects.put("minecraft:absorption", 1);
        ticks(1);
        assertEquals(List.of("absorption", "haste", "speed"), game.messages);
    }

    @Test
    void eventEffectIsTheBareNameTheOtherEffectInputsAccept() {
        assertEquals(List.of(), load("""
                on effect gain:
                    if event-effect is "speed":
                        send "fast"
                """));
        ticks(1);
        game.effects.put("minecraft:speed", 1);
        ticks(1);
        assertEquals(List.of("fast"), game.messages);
    }

    @Test
    void mountAndDismountCarryTheVehicle() {
        load("on mount:\n    send \"rode %event-entity%\"\non dismount:\n    send \"left %event-entity%\"\n");
        ticks(1);
        game.vehicle = new EntityValue("minecraft:horse", "Horse", 1, 64, 2, 0.0);
        ticks(1);
        game.vehicle = null;
        ticks(1);
        assertEquals(List.of("rode Horse", "left Horse"), game.messages);
    }

    @Test
    void swappingVehiclesDismountsThenMounts() {
        load("on mount:\n    send \"on %event-entity%\"\non dismount:\n    send \"off %event-entity%\"\n");
        game.vehicle = new EntityValue("minecraft:horse", "Horse", 1, 64, 2, 0.0);
        ticks(1);
        game.vehicle = new EntityValue("minecraft:boat", "Boat", 1, 64, 2, 0.0);
        ticks(1);
        assertEquals(List.of("off Horse", "on Boat"), game.messages);
    }

    @Test
    void dimensionChangeCarriesBothSides() {
        load("on dimension change:\n    send \"%event-from dimension% to %event-to dimension%\"\n");
        ticks(1);
        game.dimension = "minecraft:the_nether";
        ticks(1);
        game.dimension = "minecraft:the_nether";
        ticks(1);
        assertEquals(List.of("minecraft:overworld to minecraft:the_nether"), game.messages);
    }

    @Test
    void playerJoinAndLeaveFollowTheTabList() {
        load("on player join:\n    send \"+%event-player%\"\non player leave:\n    send \"-%event-player%\"\n");
        game.onlineNames.add("Steve");
        ticks(1);
        assertEquals(List.of(), game.messages);
        game.onlineNames.add("Alex");
        game.onlineNames.add("Zoe");
        ticks(1);
        game.onlineNames.remove("Steve");
        ticks(1);
        assertEquals(List.of("+Alex", "+Zoe", "-Steve"), game.messages);
    }

    @Test
    void anEmptyTabListNeverFires() {
        assertEquals(List.of(), load("on player join:\n    send \"joined\"\non player leave:\n    send \"left\"\n"));
        ticks(5);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void theFirstTickInAWorldIsStillOnlyABaseline() {
        load("on effect gain:\n    send \"effect\"\non mount:\n    send \"mount\"\non dimension change:\n    send \"dim\"\non player join:\n    send \"join\"\non world join:\n    send \"world\"\n");
        game.effects.put("minecraft:speed", 1);
        game.vehicle = new EntityValue("minecraft:horse", "Horse", 0, 0, 0, 0);
        game.dimension = "minecraft:the_end";
        game.onlineNames.add("Steve");
        ticks(2);
        assertEquals(List.of("world"), game.messages);
    }

    @Test
    void leavingAndRejoiningInANewDimensionNeverFiresDimensionChange() {
        assertEquals(List.of(), load("on dimension change:\n    send \"dim\"\non world leave:\n    send \"left\"\non world join:\n    send \"joined\"\n"));
        ticks(1);
        game.hasWorld = false;
        ticks(1);
        game.hasWorld = true;
        game.dimension = "minecraft:the_nether";
        ticks(1);
        assertEquals(List.of("joined", "left", "joined"), game.messages);
    }

    @Test
    void theNewEventValuesAreScopedToTheirEvents() {
        List<String> errors = new Parser(DefaultSyntax.registry()).parse("t.ms",
                        "on mount:\n    send \"%event-player%\"\n")
                .errors().stream().map(Object::toString).toList();
        assertEquals(List.of("t.ms:2: event-player is not available in this event"), errors);
    }
}
