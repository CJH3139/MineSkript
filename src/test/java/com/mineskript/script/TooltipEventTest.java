package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.client.inventory.elements.EffAddTooltip;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.TooltipLines;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.syntax.DefaultSyntax;
import com.mineskript.syntax.SyntaxTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;

class TooltipEventTest {
    private static final ItemValue SWORD = new ItemValue("minecraft:diamond_sword", "Diamond Sword", 1, 3, 1561,
            FakeGameBridge.Details.named("Excalibur"));

    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler());

    private void load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
        registry.replace(List.of(script));
    }

    private static List<String> errorsOf(String source) {
        return new ScriptRunner().errorsOf(source);
    }

    @Test
    void linesGoToTheBottomOrUnderTheNameInOrder() {
        load("on item tooltip:\n"
                + "    add \"&7id: %id of event-item%\" to the tooltip\n"
                + "    add \"first\" to the top of the tooltip\n"
                + "    add \"second\" to top of tooltip\n"
                + "    add \"a\" and \"b\" to tooltip\n");
        TooltipLines lines = dispatcher.onTooltip(SWORD);
        assertEquals(List.of("first", "second"), lines.top());
        assertEquals(List.of("§7id: minecraft:diamond_sword", "a", "b"), lines.bottom());
    }

    @Test
    void everyTooltipTriggerAddsItsLines() {
        load("on tooltip:\n    add \"one\" to the tooltip\non item tooltip:\n"
                + "    if custom name of event-item is set:\n        add \"renamed: %custom name of event-item%\" to the tooltip\n");
        assertEquals(List.of("one", "renamed: Excalibur"), dispatcher.onTooltip(SWORD).bottom());
        assertEquals(List.of("one"), dispatcher.onTooltip(new ItemValue("minecraft:stone", "Stone", 1, 0, 0)).bottom());
    }

    @Test
    void nothingRunsOutsideAWorld() {
        load("on item tooltip:\n    add \"x\" to the tooltip\n");
        game.hasWorld = false;
        assertTrue(dispatcher.onTooltip(SWORD).isEmpty());
    }

    @Test
    void aWaitInATooltipTriggerIsAParseError() {
        assertEquals(1, errorsOf("on item tooltip:\n    wait 1 tick\n").size());
        assertEquals(1, errorsOf("on item tooltip:\n    if 1 is 1:\n        wait until 1 is 1\n").size());
        assertEquals(1, errorsOf("on item tooltip:\n    halt until 1 is 1\n").size());
        assertEquals(1, errorsOf("on item tooltip:\n    eat held item\n").size());
        assertTrue(errorsOf("on item tooltip:\n    wait 1 tick\n").get(0).contains("can't wait"));
        assertEquals(List.of(), errorsOf("on key press of \"g\":\n    wait 1 tick\n    wait until 1 is 1\n"));
    }

    @Test
    void addToTooltipOnlyWorksInATooltipTrigger() {
        List<String> errors = errorsOf("on key press of \"g\":\n    add \"x\" to the tooltip\n");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("on item tooltip"), errors.get(0));
    }

    @Test
    void addToTooltipWinsOverTheGeneralAddEffectButLeavesOtherAddsAlone() {
        var event = SyntaxTestSupport.event("on item tooltip");
        assertInstanceOf(EffAddTooltip.class, SyntaxTestSupport.effect("add \"x\" to the tooltip", event));
        assertFalse(SyntaxTestSupport.effect("add \"x\" to {_lines::*}", event) instanceof EffAddTooltip);
        load("on item tooltip:\n    add \"x\" to {_lines::*}\n    add \"%size of {_lines::*}%\" to the tooltip\n");
        assertEquals(List.of("1"), dispatcher.onTooltip(SWORD).bottom());
    }

    @Test
    void aFunctionThatWaitsStopsTheTriggerAndKeepsEarlierLines() {
        load("function pause():\n    wait 1 tick\n\non item tooltip:\n"
                + "    add \"before\" to the tooltip\n    pause()\n    add \"after\" to the tooltip\n");
        assertEquals(List.of("before"), dispatcher.onTooltip(SWORD).bottom());
        assertEquals(List.of("before"), dispatcher.onTooltip(SWORD).bottom());
        assertEquals(1, game.errors.size(), game.errors.toString());
        assertTrue(game.errors.get(0).contains("can't wait"), game.errors.get(0));
    }

    @Test
    void anErrorIsShownOnceNotEveryFrameUntilTheScriptsReload() {
        load("on item tooltip:\n    add \"x\" to the tooltip\n    add \"%name of {_nothing}%\" to the tooltip\n");
        for (int frame = 0; frame < 10; frame++) {
            assertEquals(List.of("x"), dispatcher.onTooltip(SWORD).bottom());
        }
        assertEquals(1, game.errors.size(), game.errors.toString());
        load("on item tooltip:\n    add \"%name of {_nothing}%\" to the tooltip\n");
        dispatcher.onTooltip(SWORD);
        dispatcher.onTooltip(SWORD);
        assertEquals(2, game.errors.size());
    }

    @Test
    void aTooltipGetsAtMostSixtyFourLines() {
        load("on item tooltip:\n    loop 100 times:\n        add \"line %loop-iteration%\" to the tooltip\n");
        assertEquals(TooltipLines.MAX_LINES, dispatcher.onTooltip(SWORD).bottom().size());
    }
}
