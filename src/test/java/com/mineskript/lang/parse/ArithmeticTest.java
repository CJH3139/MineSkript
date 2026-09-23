package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArithmeticTest {
    private final ScriptRunner runner = new ScriptRunner();

    private String calc(String expression) {
        runner.game.messages.clear();
        runner.run("on load:\n    set {r} to " + expression + "\n    send {r}\n");
        return runner.game.messages.get(0);
    }

    @Test
    void precedenceAndAssociativity() {
        assertEquals("7", calc("1 + 2 * 3"));
        assertEquals("9", calc("(1 + 2) * 3"));
        assertEquals("512", calc("2 ^ 3 ^ 2"));
        assertEquals("5", calc("10 - 2 - 3"));
        assertEquals("26", calc("2 * 3 + 4 * 5"));
        assertEquals("2.5", calc("10 / 4"));
        assertEquals("-5", calc("-3 - (2)"));
        assertEquals("1", calc("2 ^ 0"));
    }

    @Test
    void variablesAndGameValuesTakePartInArithmetic() {
        runner.game.health = 7;
        runner.run("on load:\n    set {x} to 4\n    set {y} to {x} * 2 + 1\n    set {h} to health of player + 1\n    send \"%{y}% %{h}% %{x} - 1%\"\n");
        assertEquals(List.of("9 8 3"), runner.game.messages);
    }

    @Test
    void arithmeticWorksInsideConditionSlots() {
        runner.run("on load:\n    set {n} to 3\n    if {n} + 1 is 4:\n        send \"four\"\n    if 2 * 2 is {n} + 1:\n        send \"both sides\"\n    if {n} * 2 is greater than 5:\n        send \"gt\"\n");
        assertEquals(List.of("four", "both sides", "gt"), runner.game.messages);
    }

    @Test
    void listsOfSums() {
        assertEquals("2 and 4", calc("1 + 1, 2 + 2"));
    }

    @Test
    void unsetOperandIsAnError() {
        runner.run("on load:\n    set {r} to {nope} + 1\n");
        assertEquals(List.of("t.ms:2: variable is not set"), runner.game.errors);
    }

    @Test
    void textOperandIsAnError() {
        runner.run("on load:\n    set {r} to \"a\" + 1\n");
        assertEquals(List.of("t.ms:2: cannot apply + to text and number"), runner.game.errors);
    }

    @Test
    void divisionByZeroIsAnError() {
        runner.run("on load:\n    set {r} to 1 / 0\n");
        assertEquals(List.of("t.ms:2: division by zero"), runner.game.errors);
    }

    @Test
    void operatorsMustBeSeparateTokens() {
        List<String> errors = runner.errorsOf("on load:\n    set {r} to {x} -1\n");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).startsWith("t.ms:2: unknown effect"));
    }

    @Test
    void milestoneOneExpressionsStillParseUnchanged() {
        runner.game.y = 64;
        runner.game.setBlock(0, -2, 0, "minecraft:stone");
        runner.run("on load:\n    send \"%player's y-coordinate% %block 2 below player%\"\n    wait 5 ticks\n");
        assertEquals(List.of("64 stone"), runner.game.messages);
        assertEquals(List.of("wait:5"), runner.game.calls);
    }
}
