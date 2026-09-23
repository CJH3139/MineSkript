package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class LanguageAdditionsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void optionsAreSubstitutedEverywhere() {
        runner.run("options:\n    greeting: hello there\n    amount: 3\n\non load:\n    send \"{@greeting}\"\n    loop {@amount} times:\n        send \"%loop-iteration%\"\n");
        assertEquals(List.of("hello there", "1", "2", "3"), runner.game.messages);
    }

    @Test
    void anUnknownOptionIsAParseError() {
        assertEquals(List.of("t.ms:2: unknown option \"nope\""), runner.errorsOf("on load:\n    send \"{@nope}\"\n"));
    }

    @Test
    void isSetAndIsNotSet() {
        runner.run("on load:\n    if {x} is not set:\n        send \"unset\"\n    set {x} to 1\n    if {x} is set:\n        send \"set\"\n    if {_y} is set:\n        send \"wrong\"\n");
        assertEquals(List.of("unset", "set"), runner.game.messages);
    }

    @Test
    void ternaryPicksABranch() {
        runner.run("on load:\n    set {n} to 5\n    set {big} to \"big\"\n    send \"%{big} if {n} is more than 3 else {n}%\"\n    send (1 if {n} is 1 else 2)\n");
        assertEquals(List.of("big", "2"), runner.game.messages);
    }

    @Test
    void ternaryNests() {
        runner.run("on load:\n    set {n} to 2\n    send \"%10 if {n} is 1 else 20 if {n} is 2 else 30%\"\n");
        assertEquals(List.of("20"), runner.game.messages);
    }

    @Test
    void splitMakesAListYouCanLoop() {
        runner.run("on load:\n    loop \"a,b,c\" split at \",\":\n        send loop-value\n    set {s} to split \"x y\" by \" \"\n    send \"%{s}%\"\n");
        assertEquals(List.of("a", "b", "c", "x and y"), runner.game.messages);
    }

    @Test
    void eatHoldsUseUntilTheItemIsNoLongerInUse() {
        runner.game.usingItem = true;
        runner.run("on load:\n    eat held item\n    send \"done\"\n");
        assertEquals(List.of("wait:2", "waitUntil"), runner.game.calls.stream().filter(call -> call.startsWith("wait")).toList());
    }
}
