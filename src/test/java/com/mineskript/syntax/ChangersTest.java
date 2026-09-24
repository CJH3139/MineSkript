package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChangersTest {
    private final ScriptRunner runner = new ScriptRunner();

    private void run(String body) {
        runner.run("on load:\n" + body.indent(4));
    }

    private List<String> errors(String line) {
        return runner.errorsOf("on load:\n    " + line + "\n");
    }

    @Test
    void yawCanBeAddedToAndRemovedFrom() {
        runner.game.yaw = 10;
        run("add 90 to yaw\nremove 30 from the yaw\nincrease yaw by 5\nreduce yaw by 1");
        assertEquals(74.0, runner.game.yaw);
        assertEquals(List.of("yaw:100.0", "yaw:70.0", "yaw:75.0", "yaw:74.0"), runner.game.calls);
    }

    @Test
    void settingTheYawStillWorksBothWays() {
        run("set yaw to 180\nset {_y} to 45\nset the yaw to {_y}");
        assertEquals(45.0, runner.game.yaw);
    }

    @Test
    void pitchChangesStopAtStraightUpAndDown() {
        runner.game.pitch = 10;
        run("add 20 to pitch");
        assertEquals(30.0, runner.game.pitch);
        run("add 100 to pitch");
        assertEquals(90.0, runner.game.pitch);
        run("remove 500 from the pitch");
        assertEquals(-90.0, runner.game.pitch);
        run("decrease pitch by -45");
        assertEquals(-45.0, runner.game.pitch);
    }

    @Test
    void selectedSlotWrapsWithinTheHotbar() {
        run("set selected slot to 3");
        assertEquals(3, runner.game.selected);
        run("add 7 to selected slot");
        assertEquals(1, runner.game.selected);
        run("remove 2 from the selected slot");
        assertEquals(8, runner.game.selected);
        run("set selected slot to 10");
        assertEquals(1, runner.game.selected);
        run("increase selected slot by 1.4");
        assertEquals(2, runner.game.selected);
    }

    @Test
    void clipboardCanBeReadAndSet() {
        runner.game.clipboard = "copied";
        run("send clipboard\nset clipboard to \"x %1 + 1%\"\nsend \"now %the clipboard%\"\nset the clipboard to 5");
        assertEquals(List.of("copied", "now x 2"), runner.game.messages);
        assertEquals("5", runner.game.clipboard);
    }

    @Test
    void refusedModesNameWhatIsAllowed() {
        assertEquals(List.of("t.ms:2: the yaw can only be set, added to or removed from, not deleted"), errors("delete yaw"));
        assertEquals(List.of("t.ms:2: the pitch can only be set, added to or removed from, not reset"), errors("reset the pitch"));
        assertEquals(List.of("t.ms:2: the clipboard can only be set, not added to"), errors("add \"x\" to clipboard"));
        assertEquals(List.of("t.ms:2: the selected slot can only be set, added to or removed from, not used with remove all"),
                errors("remove all 1 from selected slot"));
        assertEquals(List.of("t.ms:2: {x} can only be set, added to, removed from, deleted or reset, not used with remove all"),
                errors("remove all 1 from {x}"));
    }

    @Test
    void wrongValuesAreParseErrors() {
        assertEquals(List.of("t.ms:2: cannot add text to the yaw"), errors("add \"x\" to yaw"));
        assertEquals(List.of("t.ms:2: cannot set the pitch to text"), errors("set pitch to \"up\""));
        assertEquals(List.of("t.ms:2: cannot remove text from the selected slot"), errors("remove \"x\" from selected slot"));
        assertEquals(List.of("t.ms:2: only one value can be added to the yaw"), errors("add 1 and 2 to yaw"));
        assertEquals(List.of("t.ms:2: the clipboard can only be set to one value"), errors("set clipboard to \"a\" and \"b\""));
    }

    @Test
    void thingsThatCannotBeChangedKeepTheOldError() {
        assertEquals(List.of("t.ms:2: can only set variables"), errors("set health of player to 5"));
        assertEquals(List.of("t.ms:2: can only set variables"), errors("increase health of player by 1"));
        assertEquals(List.of("t.ms:2: can only set variables"), errors("reset stone"));
    }

    @Test
    void resetDeletesVariablesAndLists() {
        run("set {x} to 1\nset {l::*} to 1 and 2\nreset {x}\nreset {l::*}\nsend \"%{x}% %size of {l::*}%\"");
        assertEquals(List.of("<none> 0"), runner.game.messages);
    }

    @Test
    void aVariableHoldingANumberChangesTheGameValue() {
        runner.game.yaw = 0;
        run("set {_turn} to 45\nadd {_turn} to yaw\nremove {_turn} from pitch");
        assertEquals(45.0, runner.game.yaw);
        assertEquals(-45.0, runner.game.pitch);
    }
}
