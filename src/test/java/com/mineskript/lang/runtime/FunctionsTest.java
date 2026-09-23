package com.mineskript.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class FunctionsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void aFunctionReturnsAValue() {
        runner.run("function double(n: number) :: number:\n    return {_n} * 2\n\non load:\n    send \"%double(21)%\"\n");
        assertEquals(List.of("42"), runner.game.messages);
    }

    @Test
    void parametersAreLocalVariables() {
        runner.run("function greet(who: text) :: text:\n    return \"hi %{_who}%\"\n\non load:\n    send greet(\"Alex\")\n");
        assertEquals(List.of("hi Alex"), runner.game.messages);
    }

    @Test
    void aFunctionCanBeCalledBeforeItIsDefined() {
        runner.run("on load:\n    send \"%square(3)%\"\n\nfunction square(n: number) :: number:\n    return {_n} * {_n}\n");
        assertEquals(List.of("9"), runner.game.messages);
    }

    @Test
    void theCallersLocalsAreHiddenAndUntouched() {
        runner.run("function poke() :: text:\n    set {_x} to \"inner\"\n    return \"%{_y}%\"\n\n"
                + "on load:\n    set {_x} to \"outer\"\n    set {_y} to \"mine\"\n    send poke()\n    send {_x}\n");
        assertEquals(List.of("<none>", "outer"), runner.game.messages);
    }

    @Test
    void aFunctionOnItsOwnLineRunsAsAnEffectAndMayWait() {
        runner.run("function slow(n: number):\n    send \"start %{_n}%\"\n    wait 2 ticks\n    send \"end %{_n}%\"\n\n"
                + "on load:\n    slow(1)\n    send \"after\"\n");
        assertEquals(List.of("start 1", "end 1", "after"), runner.game.messages);
        assertEquals(List.of("wait:2"), runner.game.calls);
    }

    @Test
    void returnWithoutAValueLeavesTheFunctionEarly() {
        runner.run("function check(n: number):\n    if {_n} is 1:\n        return\n    send \"not one\"\n\non load:\n    check(1)\n    check(2)\n");
        assertEquals(List.of("not one"), runner.game.messages);
    }

    @Test
    void returnFromInsideALoopLeavesTheLoopAndTheFunction() {
        runner.run("function first() :: number:\n    loop 5 times:\n        if loop-iteration is 3:\n            return loop-iteration\n    return 0\n\n"
                + "on load:\n    loop 2 times:\n        send \"%first()% %loop-iteration%\"\n");
        assertEquals(List.of("3 1", "3 2"), runner.game.messages);
    }

    @Test
    void recursionWorks() {
        runner.run("function fact(n: number) :: number:\n    if {_n} is at most 1:\n        return 1\n    return {_n} * fact({_n} - 1)\n\n"
                + "on load:\n    send \"%fact(5)%\"\n");
        assertEquals(List.of("120"), runner.game.messages);
    }

    @Test
    void runawayRecursionIsStoppedWithAnError() {
        runner.run("function down(n: number) :: number:\n    return down({_n} + 1)\n\non load:\n    send \"%down(0)%\"\n    send \"unreached\"\n");
        assertEquals(List.of(), runner.game.messages);
        assertEquals(1, runner.game.errors.size());
        assertEquals(true, runner.game.errors.get(0).contains("more than " + Execution.MAX_CALL_DEPTH + " calls deep"),
                runner.game.errors.get(0));
    }

    @Test
    void runawayEffectRecursionIsStoppedToo() {
        runner.run("function down():\n    down()\n\non load:\n    down()\n");
        assertEquals(1, runner.game.errors.size());
        assertEquals(true, runner.game.errors.get(0).contains("calls deep"), runner.game.errors.get(0));
    }

    @Test
    void defaultValuesFillMissingArguments() {
        runner.run("function add(a: number, b: number = 10) :: number:\n    return {_a} + {_b}\n\non load:\n    send \"%add(1)% %add(1, 2)%\"\n");
        assertEquals(List.of("11 3"), runner.game.messages);
    }

    @Test
    void waitingInsideAFunctionUsedAsAValueIsAnError() {
        runner.run("function slow() :: number:\n    wait 1 tick\n    return 1\n\non load:\n    send \"%slow()%\"\n");
        assertEquals(1, runner.game.errors.size());
        assertEquals(true, runner.game.errors.get(0).contains("can't wait"), runner.game.errors.get(0));
    }

    @Test
    void wrongArgumentCountIsAParseError() {
        assertEquals(List.of("t.ms:5: function \"one\" takes 1 argument, not 2"),
                runner.errorsOf("function one(a: number) :: number:\n    return {_a}\n\non load:\n    send \"%one(1, 2)%\"\n"));
    }

    @Test
    void usingAFunctionWithoutAReturnTypeAsAValueIsAParseError() {
        assertEquals(List.of("t.ms:5: function \"noop\" doesn't return anything, so it can't be used as a value"),
                runner.errorsOf("function noop():\n    stop\n\non load:\n    send noop()\n"));
    }

    @Test
    void returnOutsideAFunctionIsAParseError() {
        assertEquals(List.of("t.ms:2: return is only available inside a function"), runner.errorsOf("on load:\n    return 1\n"));
    }

    @Test
    void unknownFunctionIsNamed() {
        assertEquals(List.of("t.ms:2: unknown function \"nope\""), runner.errorsOf("on load:\n    nope(1)\n"));
    }

    @Test
    void duplicateFunctionsAndBadHeadersAreParseErrors() {
        assertEquals(List.of("t.ms:3: function \"a\" is already defined on line 1"),
                runner.errorsOf("function a():\n    stop\nfunction a():\n    stop\n"));
        assertEquals(List.of("t.ms:1: unknown type \"banana\""), runner.errorsOf("function a(x: banana):\n    stop\n"));
    }

    @Test
    void anErrorInsideAFunctionNamesTheFunctionsLine() {
        runner.run("function bad():\n    add \"x\" to {n}\n\non load:\n    bad()\n");
        assertEquals(List.of("t.ms:2: cannot add text to a number"), runner.game.errors);
    }
}
