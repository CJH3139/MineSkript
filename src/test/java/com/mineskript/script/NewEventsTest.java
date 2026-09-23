package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.BlockChange;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewEventsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler);

    private ParsedScript parse(String file, String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse(file, source);
        assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
        return script;
    }

    private void load(String source) {
        registry.replace(List.of(parse("t.ms", source)));
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void serverAndWorldChangeAliasesParse() {
        load("on join server:\n    send \"j\"\non leave server:\n    send \"l\"\non world change:\n    send \"w\"\non damage taken:\n    send \"d\"\n");
        assertEquals(4, registry.triggers().size());
    }

    @Test
    void healthChangeFiresBothWaysWithTheDelta() {
        load("on health change:\n    send \"%event-health change% from %event-old health%\"\n");
        ticks(1);
        game.health = 15;
        ticks(1);
        game.health = 18;
        ticks(1);
        assertEquals(List.of("-5 from 20", "3 from 15"), game.messages);
    }

    @Test
    void durabilityBelowFiresOnceWhenCrossed() {
        load("on durability below 10:\n    send \"low %event-durability%\"\n");
        game.slots[0] = new ItemValue("minecraft:iron_pickaxe", "iron pickaxe", 1, 240, 250);
        ticks(1);
        game.slots[0] = new ItemValue("minecraft:iron_pickaxe", "iron pickaxe", 1, 241, 250);
        ticks(1);
        game.slots[0] = new ItemValue("minecraft:iron_pickaxe", "iron pickaxe", 1, 242, 250);
        ticks(1);
        assertEquals(List.of("low 9"), game.messages);
    }

    @Test
    void screenOpenExposesTitleAndType() {
        load("on screen open:\n    send \"%event-screen title% %event-screen type%\"\n    send screen title\n");
        ticks(1);
        game.screenOpen = true;
        game.screenTitle = "Chest";
        game.screenType = "ContainerScreen";
        ticks(1);
        assertEquals(List.of("Chest ContainerScreen", "Chest"), game.messages);
    }

    @Test
    void blockBreakAndPlaceCarryTheBlockAndPosition() {
        load("on block break:\n    send \"broke %event-block% at %event-block x% %event-block y% %event-block z%\"\n"
                + "on block place:\n    if event-block is stone:\n        send \"placed stone\"\n");
        dispatcher.onBlockBreak(new BlockChange("minecraft:dirt", 1, 64, -2));
        dispatcher.onBlockPlace(new BlockChange("minecraft:stone", 0, 0, 0));
        assertEquals(List.of("broke dirt at 1 64 -2", "placed stone"), game.messages);
    }

    @Test
    void stopAllScriptsCancelsEveryWaitingTrigger() {
        registry.replace(List.of(
                parse("a.ms", "on chat:\n    wait 5 ticks\n    send \"a\"\n"),
                parse("b.ms", "on chat send:\n    stop all scripts\n    send \"unreached\"\n")));
        dispatcher.onChat("x");
        dispatcher.onChatSend("y");
        ticks(10);
        assertEquals(List.of(), game.messages);
    }

    @Test
    void stopScriptCancelsOnlyThatFile() {
        registry.replace(List.of(
                parse("a.ms", "on chat:\n    wait 5 ticks\n    send \"a\"\n"),
                parse("b.ms", "on chat:\n    wait 5 ticks\n    send \"b\"\n"),
                parse("c.ms", "on chat send:\n    stop script \"a\"\n    send \"still here\"\n")));
        dispatcher.onChat("x");
        dispatcher.onChatSend("y");
        ticks(10);
        assertEquals(List.of("still here", "b"), game.messages);
    }
}
