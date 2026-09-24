package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewEffectsTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void everyEffectReachesTheBridge() {
        runner.game.screenOpen = true;
        runner.run("""
                on load:
                    select slot 4
                    swap hands
                    drop item
                    drop the whole stack
                    look at location(1, 64, -3)
                    set yaw to 180
                    set pitch to -45
                    show title "hello"
                    show subtitle "there"
                    show action bar "bar"
                    play sound "minecraft:ui.button.click"
                    close screen
                """);
        assertEquals(List.of("selectSlot:4", "swapHands", "dropItem", "dropStack", "lookAt:1.0,64.0,-3.0",
                "yaw:180.0", "pitch:-45.0", "title:hello", "subtitle:there", "actionBar:bar",
                "sound:minecraft:ui.button.click", "closeScreen"), runner.game.calls);
    }

    @Test
    void effectsAcceptVariablesAndArithmetic() {
        runner.run("on load:\n    set {n} to 2\n    select slot {n} + 1\n    set {t} to \"dynamic\"\n    show title {t}\n");
        assertEquals(List.of("selectSlot:3", "title:dynamic"), runner.game.calls);
    }

    @Test
    void selectSlotChangesWhatIsHeld() {
        runner.game.setSlot(0, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        runner.game.setSlot(2, new ItemValue("minecraft:dirt", "dirt", 1, 0, 0));
        runner.run("on load:\n    send \"%held item%\"\n    select slot 2\n    send \"%held item%\"\n");
        assertEquals(List.of("stone", "dirt"), runner.game.messages);
    }

    @Test
    void theseEffectsNeedAWorld() {
        runner.game.hasWorld = false;
        runner.run("on load:\n    select slot 1\n");
        assertEquals(List.of("t.ms:2: no world"), runner.game.errors);
    }

    @Test
    void rotationEffectsBeatTheVariableSetter() {
        runner.run("on load:\n    set yaw to 90\n    set the pitch to 10\n");
        assertEquals(List.of("yaw:90.0", "pitch:10.0"), runner.game.calls);
        assertEquals(90.0, runner.game.yaw);
        assertEquals(10.0, runner.game.pitch);
        assertEquals(List.of(), runner.game.errors);
    }

    @Test
    void outOfRangeSlotNumbersAreClampedToTheHotbar() {
        runner.run("on load:\n    select hotbar slot 12\n    select slot -4\n    select slot 3.6\n");
        assertEquals(List.of("selectSlot:8", "selectSlot:0", "selectSlot:4"), runner.game.calls);
    }

    @Test
    void everyAlternateWordingParses() {
        runner.game.screenOpen = true;
        runner.run("on load:\n    swap the offhand\n    drop the item\n    drop the stack\n    show actionbar \"a\"\n    close the inventory\n");
        runner.game.screenOpen = true;
        runner.run("on load:\n    close the gui\n");
        assertEquals(List.of("swapHands", "dropItem", "dropStack", "actionBar:a", "closeScreen", "closeScreen"),
                runner.game.calls);
    }
}
