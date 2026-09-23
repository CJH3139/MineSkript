package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoopsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void timesLoopExposesIterationAndValue() {
        runner.run("on load:\n    loop 3 times:\n        send \"%loop-iteration% %loop-value%\"\n    send \"after\"\n");
        assertEquals(List.of("1 1", "2 2", "3 3", "after"), runner.game.messages);
    }

    @Test
    void onceTwiceThriceAndComputedCounts() {
        runner.run("""
                on load:
                    loop once:
                        send "a"
                    loop twice:
                        send "b"
                    loop thrice:
                        send "c"
                    set {n} to 2
                    loop {n} times:
                        send "n"
                    loop {n} + 1 times:
                        send "m"
                    loop 1 time:
                        send "t"
                """);
        assertEquals(List.of("a", "b", "b", "c", "c", "c", "n", "n", "m", "m", "m", "t"), runner.game.messages);
    }

    @Test
    void zeroNegativeAndEmptyLoopsRunNothing() {
        runner.run("""
                on load:
                    loop 0 times:
                        send "never"
                    set {n} to -2
                    loop {n} times:
                        send "never"
                    loop {unset}:
                        send "never"
                    while player is sneaking:
                        send "never"
                    send "after"
                """);
        assertEquals(List.of("after"), runner.game.messages);
    }

    @Test
    void loopOverLiteralListsAndVariables() {
        runner.run("""
                on load:
                    loop stone, dirt and gravel:
                        send loop-value
                    loop 1, 2 and 3:
                        send loop-value
                    set {l} to "a" and "b"
                    loop {l}:
                        send loop-value
                    set {s} to "solo"
                    loop {s}:
                        send loop-value
                """);
        assertEquals(List.of("stone", "dirt", "gravel", "1", "2", "3", "a", "b", "solo"), runner.game.messages);
    }

    @Test
    void loopValueWorksInConditions() {
        runner.game.setBlock(0, -1, 0, "minecraft:dirt");
        runner.run("on load:\n    loop stone, dirt and gravel:\n        if block below player is loop-value:\n            send \"on %loop-value% at pass %loop-counter%\"\n");
        assertEquals(List.of("on dirt at pass 2"), runner.game.messages);
    }

    @Test
    void whileLoopReevaluatesEachPass() {
        runner.run("on load:\n    set {i} to 0\n    while {i} is less than 3:\n        add 1 to {i}\n        send \"%{i}% %loop iteration%\"\n");
        assertEquals(List.of("1 1", "2 2", "3 3"), runner.game.messages);
    }

    @Test
    void continueSkipsTheRestOfThePass() {
        runner.run("on load:\n    loop 4 times:\n        if loop-value is 2:\n            continue\n        send loop-value\n    loop 2 times:\n        continue this loop\n        send \"never\"\n");
        assertEquals(List.of("1", "3", "4"), runner.game.messages);
    }

    @Test
    void exitLoopLeavesOnlyTheInnermostLoop() {
        runner.run("""
                on load:
                    loop 3 times:
                        loop 3 times:
                            if loop-value is 2:
                                exit loop
                            send loop-iteration
                        send "outer done"
                    loop 5 times:
                        if loop-value is 3:
                            stop loop
                        send "%loop-value%"
                    send "end"
                """);
        assertEquals(List.of("1", "outer done", "1", "outer done", "1", "outer done", "1", "2", "end"), runner.game.messages);
    }

    @Test
    void stopInsideALoopEndsTheWholeTrigger() {
        runner.run("on load:\n    loop 3 times:\n        send loop-value\n        stop\n    send \"never\"\n");
        assertEquals(List.of("1"), runner.game.messages);
    }

    @Test
    void waitInsideALoopResumesWithTheLoopState() {
        runner.run("on load:\n    loop 3 times:\n        send \"a%loop-value%\"\n        wait 1 tick\n        send \"b%loop-value%\"\n");
        assertEquals(List.of("a1", "b1", "a2", "b2", "a3", "b3"), runner.game.messages);
        assertEquals(List.of("wait:1", "wait:1", "wait:1"), runner.game.calls);
    }

    @Test
    void nestedLoopsUseTheInnermostValue() {
        runner.run("on load:\n    loop 1 and 2:\n        loop \"x\" and \"y\":\n            send \"%loop-value%\"\n");
        assertEquals(List.of("x", "y", "x", "y"), runner.game.messages);
    }

    @Test
    void runawayWhileHitsTheStepBudget() {
        runner.run("on load:\n    set {x} to 1\n    while {x} is 1:\n        send \"spin\"\n");
        assertEquals(List.of("t.ms:4: step limit exceeded"), runner.game.errors);
        assertTrue(runner.game.messages.size() > 9000);
    }

    @Test
    void whileConditionErrorsReportTheLoopLine() {
        runner.run("on load:\n    set {x} to 1\n    set {y} to \"a\"\n    while {x} is less than {y}:\n        send \"never\"\n");
        assertEquals(List.of("t.ms:4: cannot compare number with text"), runner.game.errors);
    }

    @Test
    void aWhileHeaderInsideALoopSeesTheEnclosingLoop() {
        runner.run("""
                on load:
                    loop "a" and "b":
                        set {_n} to 0
                        while loop-value is "a":
                            add 1 to {_n}
                            send "%{_n}%"
                            if {_n} is 3:
                                exit loop
                """);
        assertEquals(List.of("1", "2", "3"), runner.game.messages);
    }

    @Test
    void aListInATimesHeaderIsAParseError() {
        assertEquals(List.of("t.ms:2: loop needs a number of times or a list"), runner.errorsOf("on load:\n    loop 1, 2 times:\n        stop\n"));
        assertEquals(List.of("t.ms:2: loop needs a number of times or a list"), runner.errorsOf("on load:\n    loop 1 and 2 times:\n        stop\n"));
    }

    @Test
    void loopSyntaxErrors() {
        assertEquals(List.of("t.ms:2: loop-value is only available inside a loop"), runner.errorsOf("on load:\n    send loop-value\n"));
        assertEquals(List.of("t.ms:3: loop-value is not available inside while"), runner.errorsOf("on load:\n    while player is sneaking:\n        send loop-value\n"));
        assertEquals(List.of("t.ms:2: loop-iteration is only available inside a loop"), runner.errorsOf("on load:\n    send loop-iteration\n"));
        assertEquals(List.of("t.ms:2: continue is only available inside a loop"), runner.errorsOf("on load:\n    continue\n"));
        assertEquals(List.of("t.ms:2: exit loop is only available inside a loop"), runner.errorsOf("on load:\n    exit loop\n"));
        assertEquals(List.of("t.ms:2: loop needs a number of times or a list"), runner.errorsOf("on load:\n    loop banana:\n        stop\n"));
        assertEquals(List.of("t.ms:2: loop needs a number of times or a list"), runner.errorsOf("on load:\n    loop \"x\" times:\n        stop\n"));
        assertEquals(List.of("t.ms:2: unknown condition \"fly\""), runner.errorsOf("on load:\n    while fly:\n        stop\n"));
    }
}
