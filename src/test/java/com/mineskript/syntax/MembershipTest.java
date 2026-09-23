package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class MembershipTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void isInFindsANameInTheTabList() {
        runner.game.onlineNames.add("Steve");
        runner.game.onlineNames.add("Alex");
        runner.run("""
                on load:
                    if "Steve" is in online player names:
                        send "steve is here"
                    if "Notch" is in online player names:
                        send "notch is here"
                """);
        assertEquals(List.of("steve is here"), runner.game.messages);
    }

    @Test
    void containsReadsTheOtherWayRound() {
        runner.game.onlineNames.add("Steve");
        runner.game.onlineNames.add("Alex");
        runner.run("""
                on load:
                    if online player names contains "Alex":
                        send "alex is here"
                    if online player names contains "Notch":
                        send "notch is here"
                """);
        assertEquals(List.of("alex is here"), runner.game.messages);
    }

    @Test
    void theNegatedFormsAnswerTheOppositeWay() {
        runner.game.onlineNames.add("Steve");
        runner.game.onlineNames.add("Alex");
        runner.run("""
                on load:
                    if "Notch" is not in online player names:
                        send "notch is away"
                    if "Steve" isn't in online player names:
                        send "steve is away"
                    if online player names does not contain "Notch":
                        send "still away"
                    if online player names doesn't contain "Alex":
                        send "alex is away"
                """);
        assertEquals(List.of("notch is away", "still away"), runner.game.messages);
    }

    @Test
    void anEmptyTabListIsNeverAMatchAndItsNegationIsAlwaysOne() {
        runner.run("""
                on load:
                    if "Steve" is in online player names:
                        send "found"
                    if online player names contains "Steve":
                        send "found again"
                    if "Steve" is not in online player names:
                        send "away"
                    if online player names does not contain "Steve":
                        send "away again"
                """);
        assertEquals(List.of("away", "away again"), runner.game.messages);
    }

    @Test
    void anEmptyListIsNoLongerVacuouslyTrueInAComparison() {
        runner.run("""
                on load:
                    if "Steve" is online player names:
                        send "equal"
                    if "Nobody" is online player names:
                        send "equal too"
                    send "done"
                """);
        assertEquals(List.of("done"), runner.game.messages);
    }

    @Test
    void membershipAlsoWorksOnAListWrittenInTheScript() {
        runner.game.setBlock(0, -1, 0, "minecraft:dirt");
        runner.run("""
                on load:
                    if block below player is in stone or dirt:
                        send "soft or hard"
                    if block below player is in stone or gravel:
                        send "never"
                    if block below player is not in stone or gravel:
                        send "neither"
                """);
        assertEquals(List.of("soft or hard", "neither"), runner.game.messages);
    }

    @Test
    void theOrListOnTheRightOfAComparisonStillMeansAnyOfThem() {
        runner.game.setBlock(0, -1, 0, "minecraft:cobblestone");
        runner.run("""
                on load:
                    if block below player is stone or cobblestone:
                        send "one of them"
                    if block below player is stone or dirt:
                        send "never"
                    if block below player is not stone or dirt:
                        send "neither of them"
                    if block below player is stone and cobblestone:
                        send "never either"
                """);
        assertEquals(List.of("one of them", "neither of them"), runner.game.messages);
    }

    @Test
    void textContainsStillReachesTheTextCondition() {
        runner.run("""
                on load:
                    if "minecraft" contains "craft":
                        send "substring"
                    if "minecraft" does not contain "banana":
                        send "no banana"
                """);
        assertEquals(List.of("substring", "no banana"), runner.game.messages);
    }
}
