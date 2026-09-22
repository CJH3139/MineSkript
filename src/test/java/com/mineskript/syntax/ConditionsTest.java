package com.mineskript.syntax;

import static com.mineskript.syntax.SyntaxTestSupport.condition;
import static com.mineskript.syntax.SyntaxTestSupport.context;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.Tokenizer;
import com.mineskript.lang.runtime.Context;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConditionsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final Context context = context(game, Map.of("message", "hello"));

    private boolean test(String text) {
        return condition(text, new Event.Chat()).test(context);
    }

    private boolean parses(String text) {
        SyntaxTestSupport.SyntaxRegistryHolder holder = SyntaxTestSupport.holder();
        return holder.registry().matchFirst(holder.registry().conditions(), Tokenizer.tokenize(text), holder.parser(), SyntaxTestSupport.scope(new Event.Chat())).isPresent();
    }

    @Test
    void equalityInEverySkriptSpelling() {
        game.setBlock(0, -1, 0, "minecraft:stone");
        assertTrue(test("block below player is stone"));
        assertTrue(test("block below player = stone"));
        assertTrue(test("block below player is equal to stone"));
        assertTrue(test("block below player is the same as stone"));
        assertFalse(test("block below player is dirt"));
        assertTrue(test("block below player is not dirt"));
        assertTrue(test("block below player isn't dirt"));
        assertTrue(test("block below player != dirt"));
        assertFalse(test("block below player is not equal to stone"));
        assertTrue(test("message is \"hello\""));
        assertFalse(test("message is \"Hello\""));
    }

    @Test
    void listsOnTheRightSide() {
        game.setBlock(0, -1, 0, "minecraft:cobblestone");
        assertTrue(test("block below player is stone or cobblestone"));
        assertTrue(test("block below player is stone, dirt or cobblestone"));
        assertFalse(test("block below player is stone or dirt"));
        assertTrue(test("block below player is not stone or dirt"));
        assertFalse(test("block below player is not stone or cobblestone"));
        assertFalse(test("block below player is stone and cobblestone"));
    }

    @Test
    void orderingRelations() {
        game.health = 5;
        assertTrue(test("health of player is less than 6"));
        assertTrue(test("health of player is smaller than 6"));
        assertTrue(test("health of player is below 6"));
        assertTrue(test("health of player < 6"));
        assertFalse(test("health of player is less than 5"));
        assertTrue(test("health of player is less than or equal to 5"));
        assertTrue(test("health of player <= 5"));
        assertTrue(test("health of player is greater than 4"));
        assertTrue(test("health of player is more than 4"));
        assertTrue(test("player's health > 4"));
        assertTrue(test("health of player is greater than or equal to 5"));
        assertTrue(test("health of player >= 5"));
        assertFalse(test("health of player is above 5"));
        assertTrue(test("health of player is between 1 and 5"));
        assertTrue(test("health of player is between 5 and 9"));
        assertFalse(test("health of player is between 6 and 9"));
        assertTrue(test("health of player is not between 6 and 9"));
    }

    @Test
    void mismatchedTypesDoNotParse() {
        assertFalse(parses("player is stone"));
        assertFalse(parses("message is 5"));
        assertFalse(parses("message is greater than \"a\""));
        assertFalse(parses("stone or dirt is stone"));
    }

    @Test
    void playerStates() {
        game.sneaking = true;
        game.onGround = false;
        game.sprinting = false;
        assertTrue(test("player is sneaking"));
        assertFalse(test("player is not sneaking"));
        assertFalse(test("player is on ground"));
        assertTrue(test("player isn't on the ground"));
        assertFalse(test("player is sprinting"));
        assertTrue(test("the player is not sprinting"));
    }

    @Test
    void keyHeld() {
        game.keysDown.add("key.keyboard.left.shift");
        assertTrue(test("key \"left shift\" is held"));
        assertTrue(test("key \"shift\" is pressed"));
        assertFalse(test("key \"w\" is down"));
        assertTrue(test("key \"w\" is not held"));
        assertFalse(test("key \"shift\" isn't held"));
        SyntaxException error = assertThrows(SyntaxException.class, () -> condition("key \"banana\" is held", new Event.Load()));
        assertEquals("unknown key \"banana\"", error.getMessage());
    }
}
