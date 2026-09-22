package com.mineskript.syntax;

import static com.mineskript.syntax.SyntaxTestSupport.eval;
import static com.mineskript.syntax.SyntaxTestSupport.expr;
import static com.mineskript.syntax.SyntaxTestSupport.parser;
import static com.mineskript.syntax.SyntaxTestSupport.scope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExpressionsTest {
    private final FakeGameBridge game = new FakeGameBridge();

    @Test
    void playerIsTheLocalPlayer() {
        assertEquals(PlayerRef.LOCAL, eval("player", SkType.PLAYER, game));
        assertEquals(PlayerRef.LOCAL, eval("the player", SkType.OBJECT, game));
        assertEquals("Steve", eval("player", SkType.TEXT, game));
    }

    @Test
    void messageOnlyExistsInsideChat() {
        Object value = expr("message", SkType.TEXT, new Event.Chat()).evaluate(SyntaxTestSupport.context(game, Map.of("message", "hello")));
        assertEquals("hello", value);
        assertEquals("hello", expr("the chat message", SkType.TEXT, new Event.Chat()).evaluate(SyntaxTestSupport.context(game, Map.of("message", "hello"))));
        SyntaxException error = assertThrows(SyntaxException.class, () -> parser().parse("message", SkType.TEXT, scope(new Event.Load())));
        assertEquals("\"message\" is only available inside \"on chat\"", error.getMessage());
    }

    @Test
    void blockExpressionsReadRelativeToThePlayer() {
        game.setBlock(0, -1, 0, "minecraft:stone");
        game.setBlock(0, 2, 0, "minecraft:oak_leaves");
        game.setBlock(0, 0, -3, "minecraft:water");
        game.setBlock(1, 0, 0, "minecraft:dirt");
        assertEquals(new BlockValue("minecraft:stone"), eval("block below player", SkType.BLOCK, game));
        assertEquals(new BlockValue("minecraft:stone"), eval("the block under the player", SkType.BLOCK, game));
        assertEquals(new BlockValue("minecraft:oak_leaves"), eval("block 2 above player", SkType.BLOCK, game));
        assertEquals(new BlockValue("minecraft:oak_leaves"), eval("block 2 blocks over player", SkType.BLOCK, game));
        assertEquals(new BlockValue("minecraft:water"), eval("block 3 north of player", SkType.BLOCK, game));
        assertEquals(new BlockValue("minecraft:dirt"), eval("block east of player", SkType.BLOCK, game));
        assertEquals(new BlockValue("minecraft:air"), eval("block at player", SkType.BLOCK, game));
        assertEquals("stone", eval("block below player", SkType.TEXT, game));
    }

    @Test
    void blockExpressionsNeedAWorld() {
        game.hasWorld = false;
        ScriptError error = assertThrows(ScriptError.class, () -> eval("block below player", SkType.BLOCK, game));
        assertEquals("no world", error.getMessage());
    }

    @Test
    void playerPropertiesInBothSkriptForms() {
        game.health = 7.5;
        game.maxHealth = 20;
        game.hunger = 13;
        game.name = "Alex";
        assertEquals(7.5, eval("health of player", SkType.NUMBER, game));
        assertEquals(7.5, eval("player's health", SkType.NUMBER, game));
        assertEquals(20.0, eval("the max health of the player", SkType.NUMBER, game));
        assertEquals(13.0, eval("hunger of player", SkType.NUMBER, game));
        assertEquals(13.0, eval("player's food level", SkType.NUMBER, game));
        assertEquals("Alex", eval("name of player", SkType.TEXT, game));
        assertEquals("Alex", eval("player's name", SkType.TEXT, game));
    }

    @Test
    void coordinatesInEverySpelling() {
        game.x = 1.25;
        game.y = 64;
        game.z = -7.5;
        assertEquals(1.25, eval("x-coordinate of player", SkType.NUMBER, game));
        assertEquals(64.0, eval("player's y-coordinate", SkType.NUMBER, game));
        assertEquals(-7.5, eval("the z coordinate of the player", SkType.NUMBER, game));
        assertEquals(64.0, eval("y-coord of player", SkType.NUMBER, game));
        assertEquals(64.0, eval("player's y coord", SkType.NUMBER, game));
    }

    @Test
    void interpolationWorksWithRealExpressions() {
        game.y = 64;
        game.setBlock(0, -1, 0, "minecraft:stone");
        assertEquals("y 64 on stone", eval("\"y %player's y-coordinate% on %block below player%\"", SkType.TEXT, game));
    }

    @Test
    void unknownWordsAreNotExpressions() {
        assertTrue(parser().parse("wing span of player", SkType.NUMBER, scope(new Event.Load())).isEmpty());
    }
}
