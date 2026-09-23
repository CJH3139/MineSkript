package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeAliasTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void meAndMyselfFillAPlayerSlotInBothTheOfFormAndThePossessive() {
        runner.game.health = 7;
        runner.run("""
                on load:
                    send "%health of me%"
                    send "%me's health%"
                    send "%health of myself%"
                    send "%myself's health%"
                    send "%health of player%"
                """);
        assertEquals(List.of("7", "7", "7", "7", "7"), runner.game.messages);
    }

    @Test
    void meFillsACoordinateSlot() {
        runner.game.y = 64;
        runner.run("""
                on load:
                    send "%me's y-coordinate%"
                    send "%y-coordinate of myself%"
                """);
        assertEquals(List.of("64", "64"), runner.game.messages);
    }

    @Test
    void meIsTheSubjectOfAPlayerCondition() {
        runner.game.sneaking = true;
        runner.game.sprinting = false;
        runner.run("""
                on load:
                    if me is sneaking:
                        send "sneaking"
                    if myself is not sprinting:
                        send "not sprinting"
                    if player is sneaking:
                        send "player too"
                """);
        assertEquals(List.of("sneaking", "not sprinting", "player too"), runner.game.messages);
    }

    @Test
    void meReplacesTheBarePlayerWordInMakeSayAndMakeExecute() {
        runner.run("""
                on load:
                    make me say "hi"
                    make myself execute command "/spawn"
                    make player say "still works"
                """);
        assertEquals(List.of("chat:hi", "command:spawn", "chat:still works"), runner.game.calls);
    }

    @Test
    void meReplacesTheBarePlayerWordInTheBlockExpression() {
        runner.game.setBlock(0, -1, 0, "minecraft:stone");
        runner.run("""
                on load:
                    send "%block below me%"
                    send "%block below myself%"
                    send "%block below player%"
                    send "%block below%"
                """);
        assertEquals(List.of("stone", "stone", "stone", "stone"), runner.game.messages);
    }

    @Test
    void meOnItsOwnIsThePlayerName() {
        runner.game.name = "Steve";
        runner.run("""
                on load:
                    send "%me%"
                    send "%myself%"
                """);
        assertEquals(List.of("Steve", "Steve"), runner.game.messages);
    }

    @Test
    void meCanBeStoredInAVariableAndReadBack() {
        runner.game.name = "Steve";
        runner.run("""
                on load:
                    set {-who} to me
                    send "%{-who}%"
                """);
        assertEquals(List.of("Steve"), runner.game.messages);
    }

    @Test
    void theMessageExpressionIsUntouchedByTheNewWords() {
        assertEquals(List.of("t.ms:2: \"message\" is only available inside \"on chat\", \"on chat send\" and \"on command send\""),
                runner.errorsOf("on load:\n    send message\n"));
    }

    @Test
    void wordsThatMerelyContainMeAreUnaffected() {
        runner.game.gamemode = "creative";
        runner.run("""
                on load:
                    send "%gamemode%"
                    set {-word} to "mend"
                    send "%upper case {-word}%"
                    set {-me} to "a variable called me"
                    send "%{-me}%"
                """);
        assertEquals(List.of("creative", "MEND", "a variable called me"), runner.game.messages);
    }

    @Test
    void bothWordsWorkInBothOfTheBarePlayerEffects() {
        runner.run("""
                on load:
                    make myself say "a"
                    force me say "b"
                    make me execute command "/c"
                    let myself execute "/d"
                """);
        assertEquals(List.of("chat:a", "chat:b", "command:c", "command:d"), runner.game.calls);
    }

    @Test
    void myselfInAConditionIsTheRealPlayerAndNotABlockTypeLiteral() {
        runner.game.sneaking = true;
        runner.run("""
                on load:
                    if myself is sneaking:
                        send "yes"
                    if myself is not sneaking:
                        send "no"
                """);
        assertEquals(List.of("yes"), runner.game.messages);
    }
}
