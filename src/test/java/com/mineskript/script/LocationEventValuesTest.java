package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.BlockChange;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameSignals;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LocationEventValuesTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000),
            new Scheduler());

    private void load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
        registry.replace(List.of(script));
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void blockBreakAndPlaceGiveTheBlockLocation() {
        game.dimension = "minecraft:the_nether";
        load("on block break:\n    send \"broke at %event-location%\"\n"
                + "on block place:\n    send \"placed, x %x-coordinate of event-location%\"\n");
        ticks(1);
        dispatcher.onBlockBreak(new BlockChange("minecraft:dirt", 1, 64, -2));
        dispatcher.onBlockPlace(new BlockChange("minecraft:stone", 7, 0, 0));
        assertEquals(List.of("broke at x: 1, y: 64, z: -2 in minecraft:the_nether", "placed, x 7"), game.messages);
    }

    @Test
    void soundsAndParticlesGiveTheirLocationAndKeepTheirCoordinates() {
        load("on sound:\n    send \"%event-sound% at %event-location%, x %event-x%\"\n"
                + "on particle:\n    send \"%event-particle% at %event-location%\"\n");
        ticks(1);
        dispatcher.onSignal(new GameSignals.Signal("sound",
                Map.of("sound", "minecraft:block.note_block.harp", "x", 1.5, "y", 2.0, "z", 3.0)));
        dispatcher.onSignal(new GameSignals.Signal("particle",
                Map.of("particle", "minecraft:flame", "count", 1.0, "x", 0.0, "y", 70.0, "z", -1.0)));
        assertEquals(List.of("minecraft:block.note_block.harp at x: 1.5, y: 2, z: 3 in minecraft:overworld, x 1.5",
                "minecraft:flame at x: 0, y: 70, z: -1 in minecraft:overworld"), game.messages);
    }

    @Test
    void eventLocationIsOnlyAvailableWhereItIsDeclared() {
        List<String> errors = new Parser(DefaultSyntax.registry())
                .parse("t.ms", "on jump:\n    send \"%event-location%\"\n").errors().stream()
                .map(Object::toString).toList();
        assertEquals(List.of("t.ms:2: event-location is not available in this event"), errors);
    }
}
