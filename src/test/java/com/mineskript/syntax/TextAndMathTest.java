package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class TextAndMathTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void caseAndLength() {
        runner.run("""
                on load:
                    set {greeting} to "hello there"
                    set {shout} to "HELLO THERE"
                    set {word} to "hello"
                    send "%uppercase {greeting}%"
                    send "%lowercase {shout}%"
                    send "%length of {word}%"
                    send "%{word}'s length%"
                """);
        assertEquals(List.of("HELLO THERE", "hello there", "5", "5"), runner.game.messages);
    }

    @Test
    void substringsAreOneBasedAndInclusive() {
        runner.run("""
                on load:
                    set {word} to "minecraft"
                    send "%{word} from character 1 to 4%"
                    send "%{word} from character 5 to 9%"
                    send "%first 3 characters of {word}%"
                    send "%last 5 characters of {word}%"
                    send "%first 1 character of {word}%"
                """);
        assertEquals(List.of("mine", "craft", "min", "craft", "m"), runner.game.messages);
    }

    @Test
    void outOfRangeSubstringsClampInsteadOfThrowing() {
        runner.run("""
                on load:
                    set {word} to "abc"
                    send "%{word} from character 0 to 99%"
                    send "%{word} from character 3 to 1%"
                    send "%first 10 characters of {word}%"
                    send "%last 10 characters of {word}%"
                """);
        assertEquals(List.of("abc", "", "abc", "abc"), runner.game.messages);
    }

    @Test
    void replaceAndJoin() {
        runner.run("""
                on load:
                    set {line} to "one two two"
                    set {old} to "two"
                    set {replacement} to "three"
                    send "%{line} with {old} replaced with {replacement}%"
                    set {greeting} to "hello"
                    set {tail} to " world"
                    send "%{greeting} joined with {tail}%"
                    set {left} to "a"
                    set {right} to "b"
                    send "%{left} joined with {right}%"
                """);
        assertEquals(List.of("one three three", "hello world", "ab"), runner.game.messages);
    }

    @Test
    void textExpressionsAcceptOtherExpressionsAndVariables() {
        runner.game.name = "Steve";
        runner.run("""
                on load:
                    set {greeting} to "hi " joined with name of player
                    send "%uppercase {greeting}%"
                    send "%length of {greeting}%"
                """);
        assertEquals(List.of("HI STEVE", "8"), runner.game.messages);
    }

    @Test
    void roundingFamily() {
        runner.run("""
                on load:
                    send "%round 2.5% %round 2.4% %round -2.5%"
                    send "%floor 2.9% %ceiling 2.1%"
                    send "%absolute value of -7.5% %abs of 7.5%"
                    send "%3.14159 rounded to 2 places%"
                    send "%3.14159 rounded to 0 places%"
                """);
        assertEquals(List.of("3 2 -2", "2 3", "7.5 7.5", "3.14", "3"), runner.game.messages);
    }

    @Test
    void minMaxAndSquareRoot() {
        runner.run("""
                on load:
                    send "%minimum of 3 and 8% %maximum of 3 and 8%"
                    send "%min of -3 and -8% %max of -3 and -8%"
                    send "%square root of 16% %sqrt of 2%"
                """);
        assertEquals(List.of("3 8", "-8 -3", "4 1.41"), runner.game.messages);
    }

    @Test
    void randomNumberStaysInRangeAndAcceptsEitherOrder() {
        runner.run("""
                on load:
                    set {n} to random number between 5 and 10
                    if {n} is at least 5:
                        if {n} is at most 10:
                            send "in range"
                    set {m} to random number between 10 and 5
                    if {m} is at least 5:
                        if {m} is at most 10:
                            send "still in range"
                """);
        assertEquals(List.of("in range", "still in range"), runner.game.messages);
    }

    @Test
    void randomIntegerIsWholeAndReachesBothEnds() {
        runner.run("""
                on load:
                    loop 1000 times:
                        send "%random integer between 1 and 6%"
                """);
        assertEquals(1000, runner.game.messages.size());
        assertEquals(Set.of("1", "2", "3", "4", "5", "6"), new TreeSet<>(runner.game.messages));
    }

    @Test
    void randomIntegerAcceptsEitherOrderAndEqualBounds() {
        runner.run("""
                on load:
                    loop 1000 times:
                        send "%random integer between 6 and 1%"
                    send "%random integer between 3 and 3%"
                """);
        assertEquals(1001, runner.game.messages.size());
        assertEquals("3", runner.game.messages.get(1000));
        assertEquals(Set.of("1", "2", "3", "4", "5", "6"), new TreeSet<>(runner.game.messages));
    }

    @Test
    void atLeastAndAtMostAreInclusiveOnBothBoundaries() {
        runner.run("""
                on load:
                    if 4 is at least 5:
                        send "4 at least 5"
                    if 5 is at least 5:
                        send "5 at least 5"
                    if 6 is at least 5:
                        send "6 at least 5"
                    if 11 is at most 10:
                        send "11 at most 10"
                    if 10 is at most 10:
                        send "10 at most 10"
                    if 9 is at most 10:
                        send "9 at most 10"
                """);
        assertEquals(List.of("5 at least 5", "6 at least 5", "10 at most 10", "9 at most 10"), runner.game.messages);
    }

    @Test
    void mathsComposesWithArithmeticAndPlayerValues() {
        runner.game.health = 13.7;
        runner.run("""
                on load:
                    send "%round health of player%"
                    send "%maximum of health of player and 20%"
                    send "%round (health of player * 2)%"
                """);
        assertEquals(List.of("14", "20", "27"), runner.game.messages);
    }

    @Test
    void containsStartsWithAndEndsWith() {
        runner.run("""
                on load:
                    if "minecraft" contains "craft":
                        send "contains"
                    if "minecraft" does not contain "zzz":
                        send "not contains"
                    if "minecraft" starts with "mine":
                        send "starts"
                    if "minecraft" ends with "craft":
                        send "ends"
                    if "minecraft" doesn't start with "craft":
                        send "not starts"
                    if "minecraft" doesn't end with "mine":
                        send "not ends"
                """);
        assertEquals(List.of("contains", "not contains", "starts", "ends", "not starts", "not ends"), runner.game.messages);
    }

    @Test
    void ignoringCaseComparison() {
        runner.run("""
                on load:
                    if "Hello" is "hello" ignoring case:
                        send "same"
                    if "Hello" is not "world" ignoring case:
                        send "different"
                    if "Hello" is "hello":
                        send "plain is ignores capitals too"
                    if "Hello" is "world":
                        send "never"
                """);
        assertEquals(List.of("same", "different", "plain is ignores capitals too"), runner.game.messages);
    }

    @Test
    void textConditionsWorkOnNonTextValuesByConversion() {
        runner.game.dimension = "minecraft:the_nether";
        runner.run("""
                on load:
                    if dimension contains "nether":
                        send "in the nether"
                """);
        assertEquals(List.of("in the nether"), runner.game.messages);
    }
}
