package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.game.ClientEntityKind;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.runtime.Comparators;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.script.VariableStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocationsTest {
    private static final String OVERWORLD = "minecraft:overworld";
    private static final String NETHER = "minecraft:the_nether";

    private final ScriptRunner runner = new ScriptRunner();
    private final FakeGameBridge game = runner.game;

    private Object value(String expression) {
        runner.run("on load:\n    set {r} to " + expression + "\n");
        return runner.global("r");
    }

    private List<String> sent(String... lines) {
        runner.run("on load:\n    " + String.join("\n    ", lines) + "\n");
        return game.messages;
    }

    private static String winner(String text, SkType type) {
        Expression expression = SyntaxTestSupport.expr(text, type, new Event.Load());
        while (expression instanceof ConvertedExpression converted) {
            expression = converted.inner();
        }
        return expression.getClass().getSimpleName();
    }

    @Test
    void aLocationPrintsItsCoordinatesAndDimension() {
        Context context = new Context(game, "t.ms", Map.of());
        assertEquals("x: 1, y: 64.5, z: -3 in minecraft:overworld",
                Converters.toText(new Location(1, 64.5, -3, OVERWORLD), context));
        assertEquals(List.of("x: 1, y: 2.25, z: 3 in minecraft:the_nether"),
                sent("send \"%location(1, 2.25, 3, \"the_nether\")%\""));
        assertEquals(SkType.LOCATION, Converters.typeOf(new Location(0, 0, 0, OVERWORLD)));
        assertEquals("location", Converters.typeName(SkType.LOCATION));
    }

    @Test
    void dimensionsAreNormalisedAndMinusZeroIsZero() {
        assertEquals(NETHER, new Location(0, 0, 0, "the_nether").dimension());
        assertEquals(NETHER, new Location(0, 0, 0, " Minecraft:The_Nether ").dimension());
        assertEquals(OVERWORLD, new Location(0, 0, 0, "").dimension());
        assertEquals(new Location(0, 0, 0, OVERWORLD), new Location(-0.0, -0.0, -0.0, "overworld"));
    }

    @Test
    void locationsAreEqualOnlyWithTheSameCoordinatesAndDimension() {
        Location here = new Location(1, 2, 3, OVERWORLD);
        assertEquals(0, Comparators.relate(here, new Location(1, 2, 3, OVERWORLD)));
        assertFalse(Comparators.relate(here, new Location(1, 2, 3, NETHER)) == 0);
        assertFalse(Comparators.relate(here, new Location(1, 2, 4, OVERWORLD)) == 0);
        assertTrue(Comparators.canCompare(SkType.LOCATION, SkType.LOCATION));
        assertFalse(Comparators.canOrder(SkType.LOCATION, SkType.LOCATION));
        assertEquals(List.of("same", "different"), sent(
                "if location(1, 2, 3) is location(1, 2, 3):",
                "    send \"same\"",
                "if location(1, 2, 3) is not location(1, 2, 3, \"the_nether\"):",
                "    send \"different\""));
    }

    @Test
    void locationsSurviveSavingAndLoading(@TempDir Path dir) throws IOException {
        VariableStore store = new VariableStore();
        Path file = dir.resolve("variables.json");
        Location home = new Location(1.5, 64, -3.25, "minecraft:the_end");
        store.save(file, Map.of("home", home, "list", List.of(home, "x")));
        VariableStore.Loaded loaded = store.load(file);
        assertNull(loaded.warning());
        assertEquals(home, loaded.values().get("home"));
        assertEquals(List.of(home, "x"), loaded.values().get("list"));
        assertTrue(Files.readString(file).contains("\"type\": \"location\""));
    }

    @Test
    void aFileFromBeforeLocationsStillLoads(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("variables.json");
        Files.writeString(file, "{\"n\": {\"type\": \"number\", \"value\": 3.0}}");
        VariableStore.Loaded loaded = new VariableStore().load(file);
        assertNull(loaded.warning());
        assertEquals(Map.of("n", 3.0), loaded.values());
    }

    @Test
    void theLocationFunctionUsesTheCurrentDimensionByDefault() {
        game.dimension = NETHER;
        assertEquals(new Location(1, 2, 3, NETHER), value("location(1, 2, 3)"));
        assertEquals(new Location(1, 2, 3, "minecraft:the_end"), value("location(1, 2, 3, \"the_end\")"));
        game.hasWorld = false;
        assertEquals(new Location(4, 5, 6, OVERWORLD), value("location(4, 5, 6)"));
    }

    @Test
    void theLocationOfThePlayerAnEntityAndABlock() {
        game.dimension = NETHER;
        assertEquals(new Location(0.5, 64, -3.5, NETHER), value("location of player"));
        assertEquals(new Location(0.5, 64, -3.5, NETHER), value("player's location"));
        assertEquals(new Location(0.5, 64, -3.5, NETHER), value("the position of me"));
        game.targetEntity = new EntityValue("minecraft:zombie", "Zombie", 1, 2, 3, 4);
        assertEquals(new Location(1, 2, 3, NETHER), value("location of target entity"));
        game.targetBlock = "minecraft:stone";
        game.targetBlockPosition = new int[] {12, 40, -7};
        assertEquals(new Location(12, 40, -7, NETHER), value("target block's location"));
        assertEquals(new Location(10, 64, -4, NETHER), value("location of block at location(10.7, 64, -3.2)"));
        assertEquals(new Location(0, 63, -4, NETHER), value("location of block below player"));
    }

    @Test
    void aBlockWithoutAPositionHasNoLocation() {
        game.targetBlockPosition = null;
        runner.run("on load:\n    set {r} to location of target block\n");
        assertEquals(None.NONE, runner.global("r"));
        assertEquals(1, game.errors.size());
        assertTrue(game.errors.get(0).contains("the position of this air block is not known"), game.errors.get(0));
    }

    @Test
    void theCoordinatesOfALocation() {
        runner.run("on load:\n    set {l} to location(10, 64.5, -3)\n    set {x} to x-coordinate of {l}\n"
                + "    set {y} to {l}'s y-coord\n    set {z} to z coordinate of location(10, 64.5, -3)\n");
        assertEquals(10.0, runner.global("x"));
        assertEquals(64.5, runner.global("y"));
        assertEquals(-3.0, runner.global("z"));
    }

    @Test
    void aVariableHoldingAnEntityGivesItsCoordinatesThroughItsLocation() {
        game.targetEntity = new EntityValue("minecraft:zombie", "Zombie", 1, 2, 3, 4);
        runner.run("on load:\n    set {e} to target entity\n    set {ex} to x-coordinate of {e}\n"
                + "    set {ez} to {e}'s z-coordinate\n");
        assertEquals(1.0, runner.global("ex"));
        assertEquals(3.0, runner.global("ez"));
    }

    @Test
    void theCoordinateOfPlayerEntityAndTargetBlockKeepTheirOwnExpressions() {
        assertEquals("ExprCoordinate", winner("x-coordinate of player", SkType.NUMBER));
        assertEquals("ExprCoordinate", winner("player's y-coordinate", SkType.NUMBER));
        assertEquals("ExprEntityCoordinate", winner("x-coordinate of target entity", SkType.NUMBER));
        assertEquals("ExprTargetCoordinate", winner("x-coordinate of target block", SkType.NUMBER));
        assertEquals("ExprLocationCoordinate", winner("x-coordinate of {_l}", SkType.NUMBER));
        assertEquals("ExprLocationCoordinate", winner("{_l}'s z-coordinate", SkType.NUMBER));
        assertEquals("ExprLocationCoordinate", winner("y-coordinate of block below player", SkType.NUMBER));
    }

    @Test
    void theDistanceBetweenTwoLocations() {
        assertEquals(5.0, value("distance between location(0, 64, 0) and location(3, 68, 0)"));
        assertEquals(4.0, value("the distance between player and location(0.5, 64, 0.5)"));
        assertEquals(None.NONE, value("distance between location(0, 0, 0) and location(0, 0, 0, \"the_nether\")"));
        assertEquals(List.of("not near"), sent(
                "if distance between location(0, 0, 0) and location(0, 0, 0, \"the_nether\") is less than 5:",
                "    send \"near\"",
                "else:",
                "    send \"not near\""));
    }

    @Test
    void aLocationRelativeToAnother() {
        assertEquals(new Location(0, 66, 0, OVERWORLD), value("the location 2 above location(0, 64, 0)"));
        assertEquals(new Location(0, 64, -1, OVERWORLD), value("location north of location(0, 64, 0)"));
        assertEquals(new Location(3, 64, 0, OVERWORLD), value("the position 3 meters east of location(0, 64, 0)"));
        assertEquals(new Location(0, 63.5, 0, OVERWORLD), value("location 0.5 blocks under location(0, 64, 0)"));
        assertEquals(new Location(0.5, 62, -3.5, OVERWORLD), value("2 below player"));
        assertEquals(new Location(0.5, 64, -0.5, OVERWORLD), value("3 blocks south of player"));
        assertEquals(new Location(-1, 64, -3.5, OVERWORLD), value("1.5 west of player"));
        assertEquals(new Location(0.5, 65, -3.5, OVERWORLD), value("1 meter over me"));
        assertEquals(new Location(1, 2, 3, NETHER), value("location beneath location(1, 3, 3, \"the_nether\")"));
    }

    @Test
    void theBlockAtALocation() {
        game.setBlockAt(10, 64, 10, "minecraft:stone");
        assertEquals(List.of("stone", "air"), sent(
                "send \"%block at location(10.5, 64.9, 10.1)%\"",
                "send \"%block at location(10, 65, 10)%\""));
        assertEquals(None.NONE, value("block at location(10, 64, 10, \"the_nether\")"));
        game.setBlockAt(0, 66, -4, "minecraft:glass");
        assertEquals(List.of("stone", "air", "glass"), sent("send \"%block at 2 above player%\""));
    }

    @Test
    void blockAtPlayerIsStillTheBlockAtYourFeet() {
        assertEquals("ExprBlock", winner("block at player", SkType.BLOCK));
        assertEquals("ExprBlockAt", winner("block at {_l}", SkType.BLOCK));
        assertEquals("ExprBlockAt", winner("block at player's location", SkType.BLOCK));
    }

    @Test
    void theRelativeLocationFormsParseAsTheirOwnExpression() {
        assertEquals("ExprRelativeLocation", winner("2 above {_l}", SkType.LOCATION));
        assertEquals("ExprRelativeLocation", winner("the location 2 meters north of {_l}", SkType.LOCATION));
        assertEquals("ExprLocationOf", winner("the location of {_l}", SkType.LOCATION));
        assertEquals("ExprBlock", winner("block 2 above player", SkType.LOCATION));
    }

    @Test
    void lookAtALocation() {
        game.targetEntity = new EntityValue("minecraft:zombie", "Zombie", 1, 2, 3, 4);
        runner.run("on load:\n    look at location(1, 64, -3)\n    look at target entity\n"
                + "    look at 2 above player\n");
        assertEquals(List.of("lookAt:1.0,64.0,-3.0", "lookAt:1.0,2.0,3.0", "lookAt:0.5,66.0,-3.5"), game.calls);
    }

    @Test
    void lookingAtAnotherDimensionStopsTheLine() {
        runner.run("on load:\n    look at location(1, 64, -3, \"the_nether\")\n    send \"after\"\n");
        assertEquals(List.of(), game.calls);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of("t.ms:2: that location is in minecraft:the_nether, but you are in minecraft:overworld"),
                game.errors);
    }

    @Test
    void withinBlocksOfALocation() {
        assertEquals(List.of("near", "far", "nether is not near"), sent(
                "if player is within 5 blocks of location(0, 64, 0):",
                "    send \"near\"",
                "if player is not within 2 blocks of location(100, 64, 100):",
                "    send \"far\"",
                "if me is within 5 blocks of location(0.5, 64, -3.5, \"the_nether\"):",
                "    send \"nether is near\"",
                "if me is not within 5 blocks of location(0.5, 64, -3.5, \"the_nether\"):",
                "    send \"nether is not near\""));
    }

    @Test
    void beamsAndClientEntitiesUseLocations() {
        runner.run("on load:\n    show a \"red\" beam at player\n    show beam at location(5.5, 70, -0.2)\n"
                + "    remove beam at location(5, 70.9, -1)\n    spawn a hologram \"x\" at 2 above player\n"
                + "    move client entity last spawned client entity to location(1, 2, 3)\n");
        assertEquals(List.of(new FakeGameBridge.Beam(0, 64, -4, 0xB02E26, "t.ms")), game.beams);
        assertEquals(new FakeGameBridge.ClientEntity(ClientEntityKind.HOLOGRAM, "x", 1, 2, 3, "t.ms"),
                game.clientEntities.get(1));
        assertEquals(List.of(), game.errors);
    }

    @Test
    void visualsInAnotherDimensionStopTheLineButRemovingDoesNothing() {
        runner.run("on load:\n    show beam at location(0, 64, 0)\n    remove beam at location(0, 64, 0, \"the_end\")\n"
                + "    show beam at location(0, 64, 0, \"the_end\")\n");
        runner.run("on load:\n    spawn a hologram \"x\" at location(0, 64, 0, \"the_end\")\n");
        assertEquals(1, game.beams.size());
        assertTrue(game.clientEntities.isEmpty());
        assertEquals(List.of(
                "t.ms:4: that location is in minecraft:the_end, but you are in minecraft:overworld",
                "t.ms:2: that location is in minecraft:the_end, but you are in minecraft:overworld"), game.errors);
    }

    @Test
    void theOldNumberFormIsAParseErrorThatPointsToLocations() {
        String hint = "Positions are locations now: write location(0, 64, 0) instead of 0, 64, 0";
        for (String line : List.of("look at 0, 64, 0", "spawn a hologram \"x\" at 0, 64, 0",
                "spawn an item display of diamond at 0, 64, 0", "spawn a block display of stone at 0, 64, 0",
                "move client entity 1 to 0, 64, 0", "show a \"red\" beam at 0, 64, 0", "remove beam at 0, 64, 0",
                "set {_b} to block at 0, 64, 0")) {
            List<String> errors = runner.errorsOf("on load:\n    " + line + "\n");
            assertEquals(1, errors.size(), line);
            assertTrue(errors.get(0).endsWith(hint), errors.get(0));
        }
        List<String> errors = runner.errorsOf("on load:\n    if player is within 5 blocks of 0, 64, 0:\n"
                + "        send \"x\"\non load:\n    if block at 0, 64, 0 is stone:\n        send \"y\"\n");
        assertEquals(2, errors.size());
        assertTrue(errors.stream().allMatch(error -> error.endsWith(hint)), errors.toString());
        assertTrue(runner.errorsOf("on load:\n    look at {_x}, {_y} + 1, player's z-coordinate\n").get(0)
                .endsWith("write location({_x}, {_y} + 1, player's z-coordinate) instead of {_x}, {_y} + 1,"
                        + " player's z-coordinate"));
    }

    @Test
    void anUnrelatedErrorGetsNoLocationHint() {
        List<String> errors = runner.errorsOf("on load:\n    frobnicate 1, 2, 3\non load:\n"
                + "    look at \"a\", \"b\", \"c\"\n");
        assertEquals(2, errors.size());
        assertTrue(errors.stream().noneMatch(error -> error.contains("Positions are locations")), errors.toString());
    }
}
