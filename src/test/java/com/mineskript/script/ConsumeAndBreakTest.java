package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConsumeAndBreakTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
    });

    private List<String> load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        registry.replace(List.of(script));
        return script.errors().stream().map(Object::toString).toList();
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    private void startEating(ItemValue food, int duration) {
        game.usingItem = true;
        game.consumingItem = true;
        game.useItem = food;
        game.useItemRemaining = duration;
    }

    private void stopUsing() {
        game.usingItem = false;
        game.consumingItem = false;
        game.useItem = ItemValue.empty();
        game.useItemRemaining = 0;
    }

    @Test
    void consumeFiresWhenAConsumableUseRunsToCompletion() {
        load("on consume:\n    send \"ate %event-item%\"\n");
        game.setSlot(0, new ItemValue("minecraft:bread", "bread", 3, 0, 0));
        ticks(1);
        startEating(new ItemValue("minecraft:bread", "bread", 3, 0, 0), 32);
        ticks(1);
        game.useItemRemaining = 16;
        ticks(1);
        game.useItemRemaining = 1;
        ticks(1);
        assertEquals(List.of(), game.messages);
        stopUsing();
        game.setSlot(0, new ItemValue("minecraft:bread", "bread", 2, 0, 0));
        ticks(1);
        assertEquals(List.of("ate 3 bread"), game.messages);
    }

    @Test
    void aUseReleasedEarlyDoesNotFireConsume() {
        assertEquals(List.of(), load("on consume:\n    send \"ate\"\n"));
        ticks(1);
        startEating(new ItemValue("minecraft:bread", "bread", 1, 0, 0), 32);
        ticks(1);
        game.useItemRemaining = 20;
        ticks(1);
        stopUsing();
        ticks(1);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void theConsumeThresholdFiresAtTwoTicksLeftAndNotAtThree() {
        load("on consume:\n    send \"ate %event-item%\"\n");
        ticks(1);
        startEating(new ItemValue("minecraft:bread", "bread", 1, 0, 0), 32);
        ticks(1);
        game.useItemRemaining = 3;
        ticks(1);
        stopUsing();
        ticks(1);
        assertEquals(List.of(), game.messages);
        startEating(new ItemValue("minecraft:apple", "apple", 1, 0, 0), 32);
        ticks(1);
        game.useItemRemaining = 2;
        ticks(1);
        stopUsing();
        ticks(1);
        assertEquals(List.of("ate apple"), game.messages);
    }

    @Test
    void drawingABowToFullDoesNotFireConsume() {
        load("on consume:\n    send \"ate\"\non stop using item:\n    send \"stopped\"\n");
        ticks(1);
        game.usingItem = true;
        game.consumingItem = false;
        game.useItem = new ItemValue("minecraft:bow", "bow", 1, 0, 384);
        game.useItemRemaining = 1;
        ticks(1);
        stopUsing();
        ticks(1);
        assertEquals(List.of("stopped"), game.messages);
    }

    @Test
    void consumeReadsTheItemFromBeforeNotAfter() {
        load("on consume:\n    send \"%event-item%\"\n");
        game.setSlot(0, new ItemValue("minecraft:mushroom_stew", "mushroom stew", 1, 0, 0));
        ticks(1);
        startEating(new ItemValue("minecraft:mushroom_stew", "mushroom stew", 1, 0, 0), 32);
        ticks(1);
        game.useItemRemaining = 0;
        ticks(1);
        stopUsing();
        game.setSlot(0, new ItemValue("minecraft:bowl", "bowl", 1, 0, 0));
        ticks(1);
        assertEquals(List.of("mushroom stew"), game.messages);
    }

    @Test
    void itemBreakFiresWhenAnAlmostBrokenHeldToolBecomesAir() {
        load("on item break:\n    send \"broke %event-item%\"\n");
        game.setSlot(0, new ItemValue("minecraft:wooden_pickaxe", "wooden pickaxe", 1, 58, 59));
        ticks(1);
        game.setSlot(0, null);
        ticks(1);
        assertEquals(List.of("broke wooden pickaxe"), game.messages);
    }

    @Test
    void aToolWithDurabilityLeftThatVanishesIsNotABreak() {
        assertEquals(List.of(), load("on item break:\n    send \"broke\"\n"));
        game.setSlot(0, new ItemValue("minecraft:wooden_pickaxe", "wooden pickaxe", 1, 10, 59));
        ticks(1);
        game.setSlot(0, null);
        ticks(1);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void anUndamageableStackThatVanishesIsNotABreak() {
        assertEquals(List.of(), load("on item break:\n    send \"broke\"\n"));
        game.setSlot(0, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        ticks(1);
        game.setSlot(0, null);
        ticks(1);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void scrollingAwayFromAnAlmostBrokenToolIsNotABreak() {
        assertEquals(List.of(), load("on item break:\n    send \"broke\"\n"));
        game.setSlot(0, new ItemValue("minecraft:wooden_pickaxe", "wooden pickaxe", 1, 58, 59));
        ticks(1);
        game.selected = 1;
        ticks(1);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void itemBreakReadsTheItemFromBeforeNotAfter() {
        load("on item break:\n    send \"%id of event-item% %damage of event-item%\"\n");
        game.setSlot(0, new ItemValue("minecraft:iron_axe", "iron axe", 1, 249, 250));
        ticks(1);
        game.setSlot(0, null);
        ticks(1);
        assertEquals(List.of("minecraft:iron_axe 249"), game.messages);
    }

    @Test
    void xpChangeFiresOnlyWhenTheLevelHoldsStill() {
        load("on xp change:\n    send \"xp %event-xp change%\"\non level change:\n    send \"level %event-level change%\"\n");
        game.xpLevel = 5;
        game.totalExperience = 100;
        ticks(1);
        game.totalExperience = 112;
        ticks(1);
        game.totalExperience = 106;
        ticks(1);
        game.xpLevel = 6;
        game.totalExperience = 130;
        ticks(1);
        assertEquals(List.of("xp 12", "xp -6", "level 1"), game.messages);
    }

    @Test
    void theEventValuesOfTheseEventsAreScoped() {
        List<String> errors = new Parser(DefaultSyntax.registry()).parse("t.ms", "on load:\n    send \"%event-xp change%\"\n")
                .errors().stream().map(Object::toString).toList();
        assertEquals(List.of("t.ms:2: event-xp change is not available in this event"), errors);
    }
}
