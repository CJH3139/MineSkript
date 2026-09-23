package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.mineskript.ScriptRunner;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class CombinedConditionsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void inlineAndOr() {
        runner.game.sneaking = true;
        runner.game.keysDown.add("key.keyboard.w");
        runner.run("""
                on load:
                    if player is sneaking and key "w" is held:
                        send "both"
                    if player is sneaking and key "s" is held:
                        send "never"
                    if player is sprinting or key "w" is held:
                        send "either"
                    if player is sprinting or key "s" is held:
                        send "neither"
                """);
        assertEquals(List.of("both", "either"), runner.game.messages);
    }

    @Test
    void andBindsTighterThanOrAndParensGroup() {
        runner.game.sneaking = true;
        runner.game.onGround = false;
        runner.run("""
                on load:
                    if player is on ground or player is sneaking and player is not sprinting:
                        send "or-and"
                    if (player is on ground or player is sneaking) and player is sprinting:
                        send "never"
                    if (player is on ground or player is sneaking) and player is not sprinting:
                        send "paren"
                """);
        assertEquals(List.of("or-and", "paren"), runner.game.messages);
    }

    @Test
    void listsAndRangesStillParseAsSingleConditions() {
        runner.game.setBlock(0, -1, 0, "minecraft:dirt");
        runner.game.health = 5;
        runner.run("""
                on load:
                    if block below player is stone or dirt:
                        send "list"
                    if health of player is between 1 and 9:
                        send "range"
                    if block below player is stone or dirt and health of player is between 1 and 9:
                        send "combined"
                """);
        assertEquals(List.of("list", "range", "combined"), runner.game.messages);
    }

    @Test
    void variablesCombine() {
        runner.run("""
                on load:
                    set {hp} to 3
                    set {food} to 20
                    if {hp} is less than 6 or {food} is less than 6:
                        send "low"
                    if {hp} is less than 6 and {food} is less than 6:
                        send "never"
                """);
        assertEquals(List.of("low"), runner.game.messages);
    }

    @Test
    void anUnknownAtomFailsTheWholeLine() {
        assertEquals(List.of("t.ms:2: unknown condition \"player is sneaking and player is nonsense\""),
                runner.errorsOf("on load:\n    if player is sneaking and player is nonsense:\n        stop\n"));
        assertEquals(List.of("t.ms:2: unknown condition \"player is sneaking and\""),
                runner.errorsOf("on load:\n    if player is sneaking and:\n        stop\n"));
    }

    @Test
    void ifAllAndIfAnySections() {
        runner.game.sneaking = true;
        runner.game.health = 5;
        runner.run("""
                on load:
                    if all:
                        player is sneaking
                        health of player is less than 6
                    then:
                        send "all"
                    if all:
                        player is sneaking
                        health of player is greater than 6
                    then:
                        send "never"
                    if any:
                        player is sprinting
                        health of player is less than 6
                    then:
                        send "any"
                    if any:
                        player is sprinting
                        health of player is greater than 6
                    then:
                        send "never either"
                """);
        assertEquals(List.of("all", "any"), runner.game.messages);
    }

    @Test
    void sectionsChainWithElseIfAndElse() {
        runner.game.health = 5;
        runner.run("""
                on load:
                    if all:
                        player is sneaking
                        player is sprinting
                    then:
                        send "a"
                    else if any:
                        health of player is less than 6
                        hunger of player is less than 6
                    then:
                        send "b"
                    else:
                        send "c"
                    send "done"
                """);
        assertEquals(List.of("b", "done"), runner.game.messages);
        runner.game.messages.clear();
        runner.game.health = 20;
        runner.run("""
                on load:
                    if player is sneaking:
                        send "a"
                    else if all:
                        health of player is 20
                        hunger of player is 20
                    then:
                        send "full"
                    else:
                        send "c"
                """);
        assertEquals(List.of("full"), runner.game.messages);
    }

    @Test
    void aLongFailingChainStillParsesQuickly() {
        StringBuilder line = new StringBuilder("on load:\n    if player is sneaking");
        for (int i = 0; i < 16; i++) {
            line.append(" and player is sneaking");
        }
        line.append(" and bogus condition here:\n        stop\n");
        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertEquals(1, runner.errorsOf(line.toString()).size()));
    }

    @Test
    void sectionErrors() {
        assertEquals(List.of("t.ms:2: \"if any\" needs at least two conditions"),
                runner.errorsOf("on load:\n    if any:\n        player is sneaking\n    then:\n        stop\n"));
        assertEquals(List.of("t.ms:2: \"if all\" must be followed by \"then\""),
                runner.errorsOf("on load:\n    if all:\n        player is sneaking\n        player is sprinting\n    send \"x\"\n"));
        assertEquals(List.of("t.ms:2: \"if all\" must be followed by \"then\""),
                runner.errorsOf("on load:\n    if all:\n        player is sneaking\n        player is sprinting\n"));
        assertEquals(List.of("t.ms:2: \"then\" without \"if all\" or \"if any\""),
                runner.errorsOf("on load:\n    then:\n        stop\n"));
        assertEquals(List.of("t.ms:3: \"if all\" may not contain sections"),
                runner.errorsOf("on load:\n    if all:\n        if player is sneaking:\n            stop\n        player is sprinting\n    then:\n        stop\n"));
        assertEquals(List.of("t.ms:3: unknown condition \"fly\""),
                runner.errorsOf("on load:\n    if any:\n        fly\n        player is sneaking\n    then:\n        stop\n"));
    }
}
