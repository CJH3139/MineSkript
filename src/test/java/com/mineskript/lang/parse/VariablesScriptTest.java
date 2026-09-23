package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.ast.None;
import java.util.List;
import org.junit.jupiter.api.Test;

class VariablesScriptTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void setAndReadAGlobal() {
        runner.run("on load:\n    set {count} to 5\n    send \"count is %{count}%\"\n    send {count}\n");
        assertEquals(List.of("count is 5", "5"), runner.game.messages);
        assertEquals(5.0, runner.global("count"));
    }

    @Test
    void unsetPrintsNone() {
        runner.run("on load:\n    send \"%{nothing}%\"\n    send {nothing}\n");
        assertEquals(List.of("<none>", "<none>"), runner.game.messages);
        assertSame(None.NONE, runner.global("nothing"));
    }

    @Test
    void addAndRemoveInEverySpellingTreatUnsetAsZero() {
        runner.run("on load:\n    add 1 to {x}\n    give 2 to {x}\n    increase {x} by 1\n    remove 0.5 from {x}\n    subtract 1 from {x}\n    reduce {x} by 1\n    decrease {x} by 0.5\n    send {x}\n");
        assertEquals(List.of("1"), runner.game.messages);
    }

    @Test
    void deleteAndClearRemoveTheValue() {
        runner.run("on load:\n    set {a} to 1\n    set {b} to 2\n    delete {a}\n    clear {b}\n    send \"%{a}% %{b}%\"\n");
        assertEquals(List.of("<none> <none>"), runner.game.messages);
    }

    @Test
    void addingToTextIsARuntimeErrorAtTheRightLine() {
        runner.run("on load:\n    set {x} to \"hi\"\n    add 1 to {x}\n    send \"unreached\"\n");
        assertEquals(List.of("t.ms:3: cannot add a number to text"), runner.game.errors);
        assertEquals(List.of(), runner.game.messages);
    }

    @Test
    void addingTextIsARuntimeError() {
        runner.run("on load:\n    add \"hi\" to {x}\n");
        assertEquals(List.of("t.ms:2: cannot add text to a number"), runner.game.errors);
        runner.game.errors.clear();
        runner.run("on load:\n    remove \"hi\" from {x}\n");
        assertEquals(List.of("t.ms:2: cannot remove text from a number"), runner.game.errors);
    }

    @Test
    void threeScopesAreDistinct() {
        runner.run("on load:\n    set {x} to 1\n    set {-x} to 2\n    set {_x} to 3\n    send \"%{x}% %{-x}% %{_x}%\"\n");
        assertEquals(List.of("1 2 3"), runner.game.messages);
    }

    @Test
    void localsDieWithTheTriggerButGlobalsAndRamCarryOver() {
        runner.run("on load:\n    set {_l} to \"a\"\n    set {g} to \"b\"\n    set {-r} to \"c\"\n\non load:\n    send \"%{_l}% %{g}% %{-r}%\"\n");
        assertEquals(List.of("<none> b c"), runner.game.messages);
    }

    @Test
    void globalsAndRamSurviveAcrossSeparateRuns() {
        runner.run("on load:\n    set {g} to 1\n    set {-r} to 2\n");
        runner.run("on load:\n    send \"%{g}% %{-r}%\"\n");
        assertEquals(List.of("1 2"), runner.game.messages);
    }

    @Test
    void variablesFeedTypedSlots() {
        runner.run("on load:\n    set {d} to 5 ticks\n    wait {d}\n    set {t} to \"hello\"\n    make player say {t}\n");
        assertEquals(List.of("wait:5", "chat:hello"), runner.game.calls);
    }

    @Test
    void unsetInATypedSlotIsARuntimeError() {
        runner.run("on load:\n    wait {nope}\n");
        assertEquals(List.of("t.ms:2: variable is not set"), runner.game.errors);
    }

    @Test
    void comparisonsWithVariables() {
        runner.run("""
                on load:
                    set {x} to 5
                    if {x} is 5:
                        send "eq"
                    if {x} is less than 10:
                        send "lt"
                    if {y} is not 5:
                        send "ne"
                    if {y} is less than 5:
                        send "never"
                    if {y} is between 1 and 9:
                        send "never either"
                    if {x} is 4 or 5:
                        send "list"
                """);
        assertEquals(List.of("eq", "lt", "ne", "list"), runner.game.messages);
    }

    @Test
    void storedBlockComparesWithABlockType() {
        runner.game.setBlock(0, -1, 0, "minecraft:stone");
        runner.run("on load:\n    set {b} to block below player\n    if {b} is stone:\n        send \"yes: %{b}%\"\n");
        assertEquals(List.of("yes: stone"), runner.game.messages);
    }

    @Test
    void namesAreCaseInsensitiveAndMayContainSpaces() {
        runner.run("on load:\n    set {My Counter} to 1\n    send {my counter}\n    send {MY   COUNTER}\n");
        assertEquals(List.of("1", "1"), runner.game.messages);
    }

    @Test
    void listsCanBeStoredAndPrinted() {
        runner.run("on load:\n    set {l} to stone, dirt and gravel\n    send {l}\n");
        assertEquals(List.of("stone, dirt and gravel"), runner.game.messages);
    }

    @Test
    void onlyVariablesCanBeChanged() {
        assertEquals(List.of("t.ms:2: can only set variables"), runner.errorsOf("on load:\n    set health of player to 5\n"));
        assertEquals(List.of("t.ms:2: can only set variables"), runner.errorsOf("on load:\n    add 1 to health of player\n"));
        assertEquals(List.of("t.ms:2: can only set variables"), runner.errorsOf("on load:\n    delete stone\n"));
    }

    @Test
    void emptyVariableNameIsAParseError() {
        assertEquals(List.of("t.ms:2: empty variable name"), runner.errorsOf("on load:\n    set {} to 1\n"));
        assertEquals(List.of("t.ms:2: empty variable name"), runner.errorsOf("on load:\n    send {_}\n"));
    }
}
