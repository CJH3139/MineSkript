package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScriptCommandTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler,
            new Variables(), () -> {
            });

    private List<String> load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        registry.replace(List.of(script));
        dispatcher.refreshCommands();
        dispatcher.tick();
        return script.errors().stream().map(Object::toString).toList();
    }

    @Test
    void aCommandRunsItsTriggerAndIsNotSentToTheServer() {
        assertEquals(List.of(), load("""
                command /hello:
                    trigger:
                        send "hi there"
                """));
        assertFalse(dispatcher.onCommandSend("hello"));
        assertEquals(List.of("hi there"), game.messages);
        assertTrue(dispatcher.onCommandSend("spawn"));
    }

    @Test
    void argumentsAreTypedAndNumbered() {
        assertEquals(List.of(), load("""
                command /add <number> <number>:
                    trigger:
                        send "%arg-1 + arg-2%"
                        send "%arg 2%"
                        send "%the 1st argument%"
                        send "%last arg%"
                        send "%size of arguments%"
                """));
        dispatcher.onCommandSend("add 2 3.5");
        assertEquals(List.of("5.5", "3.5", "2", "3.5", "2"), game.messages);
    }

    @Test
    void textArgumentsTakeWordsAndTheLastOneTakesTheRest() {
        load("""
                command /msg <player> <text>:
                    trigger:
                        send "to %arg-1%: %arg-2%"
                """);
        dispatcher.onCommandSend("msg Steve hello there friend");
        assertEquals(List.of("to Steve: hello there friend"), game.messages);
    }

    @Test
    void optionalArgumentsAreNoneOrTheirDefault() {
        load("""
                command /greet [<text=friend>]:
                    trigger:
                        send "hello %arg%"

                command /count [<number>]:
                    trigger:
                        if arg is set:
                            send "%arg%"
                        else:
                            send "none"
                """);
        dispatcher.onCommandSend("greet");
        dispatcher.onCommandSend("greet Alex");
        dispatcher.onCommandSend("count");
        dispatcher.onCommandSend("count 4");
        assertEquals(List.of("hello friend", "hello Alex", "none", "4"), game.messages);
    }

    @Test
    void wrongArgumentsShowTheUsage() {
        load("""
                command /boost <number>:
                    trigger:
                        send "ok"

                command /tp2 <number>:
                    usage: &cUse /tp2 and a number
                    trigger:
                        send "ok"
                """);
        assertFalse(dispatcher.onCommandSend("boost fast"));
        assertFalse(dispatcher.onCommandSend("tp2"));
        assertEquals(List.of("Correct usage: /boost <number>", "§cUse /tp2 and a number"), game.messages);
    }

    @Test
    void aliasesLabelsAndCapitalsAllWork() {
        load("""
                command /coordinates:
                    aliases: /coords, c
                    trigger:
                        send "at you"
                """);
        dispatcher.onCommandSend("coords");
        dispatcher.onCommandSend("C");
        dispatcher.onCommandSend("Coordinates");
        assertEquals(List.of("at you", "at you", "at you"), game.messages);
        assertEquals(List.of("coordinates", "coords", "c"), game.scriptCommands);
    }

    @Test
    void aCooldownBlocksUseUntilItRunsOut() {
        load("""
                command /heal:
                    cooldown: 2 seconds
                    cooldown message: wait %remaining time%
                    trigger:
                        send "healed"
                """);
        dispatcher.onCommandSend("heal");
        dispatcher.onCommandSend("heal");
        for (int i = 0; i < 40; i++) {
            dispatcher.tick();
        }
        dispatcher.onCommandSend("heal");
        assertEquals(List.of("healed", "wait 2 seconds", "healed"), game.messages);
    }

    @Test
    void onCommandSendCanCancelOrRewriteAScriptCommandFirst() {
        load("""
                on command send:
                    if message is "blocked":
                        cancel event
                    if message is "h":
                        set message to "hello"

                command /hello:
                    trigger:
                        send "hi"

                command /blocked:
                    trigger:
                        send "never"
                """);
        assertFalse(dispatcher.onCommandSend("h"));
        assertFalse(dispatcher.onCommandSend("blocked"));
        assertEquals(List.of("hi"), game.messages);
    }

    @Test
    void aCommandTriggerCanWait() {
        load("""
                command /later:
                    trigger:
                        send "one"
                        wait 2 ticks
                        send "two"
                """);
        dispatcher.onCommandSend("later");
        assertEquals(List.of("one"), game.messages);
        dispatcher.tick();
        dispatcher.tick();
        dispatcher.tick();
        assertEquals(List.of("one", "two"), game.messages);
    }

    @Test
    void mistakesAreReportedWhenTheScriptLoads() {
        assertEquals(List.of("t.ms:1: /ms is MineSkript's own command and can't be replaced"),
                load("command /ms:\n    trigger:\n        send \"x\"\n"));
        assertEquals(List.of("t.ms:1: the command /x has no trigger: put its code under \"trigger:\""),
                load("command /x:\n    usage: hi\n"));
        assertEquals(List.of("t.ms:2: unknown command entry \"colour\"; a command can have aliases, usage, description,"
                        + " cooldown, cooldown message and a trigger"),
                load("command /x:\n    colour: red\n    trigger:\n        send \"x\"\n"));
        assertEquals(List.of("t.ms:3: /x has more than one argument, so say which one: arg-1, arg-2 and so on"),
                load("command /x <number> <number>:\n    trigger:\n        send \"%arg%\"\n"));
        assertEquals(List.of("t.ms:2: arguments are only available inside a command's trigger"),
                load("on load:\n    send \"%arg-1%\"\n"));
        assertEquals(List.of("t.ms:4: /x is defined twice in this script"),
                load("command /x:\n    trigger:\n        send \"a\"\ncommand /x:\n    trigger:\n        send \"b\"\n"));
        assertEquals(List.of("t.ms:1: the default value \"abc\" is not a number"),
                load("command /x [<number=abc>]:\n    trigger:\n        send \"a\"\n"));
    }
}
