package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParseTest {
    private final ScriptRunner runner = new ScriptRunner();

    private List<String> messages(String body) {
        runner.run("on load:\n" + body.indent(4));
        return runner.game.messages;
    }

    @Test
    void aPatternPullsEveryValueOutInOrder() {
        assertEquals(List.of("2", "5", "x Booster", "big"), messages("""
                set {_parts::*} to "5x Booster" parsed as "%number%%string%"
                send "%size of {_parts::*}%"
                send "%{_parts::1}%"
                send "%{_parts::2}%"
                if {_parts::1} > 3:
                    send "big"
                """));
    }

    @Test
    void literalTextInThePatternIgnoresCapitalsAndSpacing() {
        assertEquals(List.of("Steve", "diamond", "64"), messages("""
                set {_trade::*} to "Steve IS SELLING  diamond for 64 coins" parsed as "%string% is selling %itemtype% for %number% coins"
                send "%{_trade::1}%"
                send "%{_trade::2}%"
                send "%{_trade::3}%"
                """));
    }

    @Test
    void optionalPartsAndChoicesWork() {
        assertEquals(List.of("3", "3", "7"), messages("""
                set {_a} to "buy 3 apples" parsed as "(buy|purchase) [a lot of ]%integer% apples"
                set {_b} to "buy a lot of 3 apples" parsed as "(buy|purchase) [a lot of ]%integer% apples"
                set {_c} to "purchase 7 apples" parsed as "(buy|purchase) [a lot of] %integer% apples"
                send "%{_a}%"
                send "%{_b}%"
                send "%{_c}%"
                """));
    }

    @Test
    void aPluralTypeReadsAListAndFlattensIt() {
        assertEquals(List.of("4", "1", "2", "3", "Bob"), messages("""
                set {_n::*} to "scores: 1, 2 and 3 points for Bob" parsed as "scores: %numbers% points for %string%"
                send "%size of {_n::*}%"
                send "%{_n::1}%"
                send "%{_n::2}%"
                send "%{_n::3}%"
                send "%{_n::4}%"
                """));
    }

    @Test
    void textThatDoesNotFitGivesNothingAndSetsTheParseError() {
        assertEquals(List.of("0", "hello could not be parsed as \"%number% coins\"", "no error"), messages("""
                set {_x::*} to "hello" parsed as "%number% coins"
                send "%size of {_x::*}%"
                send "%parse error%"
                set {_y::*} to "5 coins" parsed as "%number% coins"
                if the last parse error is not set:
                    send "no error"
                """));
    }

    @Test
    void typesCanBeNamedWithoutAPattern() {
        assertEquals(List.of("creative", "zombie", "not an item", "diamond_sword", "abc could not be parsed as a gamemode"), messages("""
                set {_g} to "CREATIVE" parsed as gamemode
                send "%{_g}%"
                set {_e} to "zombie" parsed as an entity type
                send "%{_e}%"
                if "two, words" parsed as item type is not set:
                    send "not an item"
                send "%"diamond sword" parsed as item type%"
                set {_m} to "abc" parsed as gamemode
                send "%parse error%"
                """));
    }

    @Test
    void theOlderParsedAsFormsSetTheParseErrorToo() {
        assertEquals(List.of("abc could not be parsed as a number", "1.5 could not be parsed as an integer"), messages("""
                set {_n} to "abc" parsed as number
                send "%parse error%"
                set {_n} to "1.5" parsed as integer
                send "%parse error%"
                """));
    }

    @Test
    void aBrokenPatternIsAnErrorWhenTheScriptLoads() {
        List<String> errors = runner.errorsOf("on load:\n    set {_x::*} to \"a\" parsed as \"%player% hi\"\n");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("can't parse with the pattern \"%player% hi\""), errors.get(0));
        assertTrue(runner.errorsOf("on load:\n    set {_x::*} to \"a\" parsed as \"[oops\"\n").get(0)
                .contains("never closed"));
    }
}
