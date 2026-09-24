package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class StandaloneConditionTest {
    private final ScriptRunner runner = new ScriptRunner();

    private List<String> messages(String source) {
        runner.run(source);
        return runner.game.messages;
    }

    @Test
    void aFailingConditionStopsTheTrigger() {
        assertEquals(List.of("a", "b"), messages("""
                on load:
                    send "a"
                    1 is 1
                    send "b"
                    2 is 3
                    send "never"
                """));
    }

    @Test
    void insideAnIfItSkipsTheRestOfThatSectionOnly() {
        assertEquals(List.of("in", "after"), messages("""
                on load:
                    if 1 is 1:
                        send "in"
                        "a" is "b"
                        send "never"
                    else:
                        send "else never"
                    send "after"
                """));
    }

    @Test
    void insideALoopItMovesOnToTheNextIteration() {
        assertEquals(List.of("2", "4", "done"), messages("""
                on load:
                    loop 4 times:
                        mod(loop-iteration, 2) is 0
                        send "%loop-iteration%"
                    send "done"
                """));
    }

    @Test
    void insideAFunctionItEndsTheFunction() {
        assertEquals(List.of("start", "<none>", "yes"), messages("""
                function check(n: number) :: text:
                    {_n} > 3
                    return "yes"

                on load:
                    send "start"
                    send "%check(1)%"
                    send "%check(5)%"
                """));
    }

    @Test
    void theBossBarExampleParses() {
        assertEquals(List.of(), runner.errorsOf("""
                on bossbar update:
                    event-bossbar change is "name"
                    set {_parts::*} to event-text parsed as "%number%%string%"
                    {_parts::1} > 3
                    loop 10 times:
                        play sound "minecraft:block.anvil.land"
                        wait a tick
                """));
    }

    @Test
    void aLineThatIsNeitherIsStillAnUnknownEffect() {
        assertEquals(List.of("t.ms:2: unknown effect \"fly away\""), runner.errorsOf("on load:\n    fly away\n"));
    }
}
