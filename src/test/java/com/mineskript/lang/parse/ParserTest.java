package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.IfChain;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Execution;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParserTest {
    private final Parser parser = new Parser(DefaultSyntax.registry());
    private final FakeGameBridge game = new FakeGameBridge();

    private ParsedScript parse(String source) {
        return parser.parse("test.ms", source);
    }

    private void run(Trigger trigger, Map<String, Object> values) {
        Interpreter interpreter = new Interpreter(1000);
        Execution execution = new Execution(trigger, new Context(game, "test.ms", values));
        while (interpreter.run(execution) == Interpreter.Outcome.WAITING) {
            if (execution.waitCondition() != null) {
                game.calls.add("waitUntil");
                break;
            }
            game.calls.add("wait:" + execution.waitTicks());
        }
    }

    @Test
    void parsesTheReferenceScript() {
        ParsedScript script = parse("""
                on key press of "r":
                    if block below player is stone or cobblestone:
                        send "on stone, mining at y %player's y-coordinate%"
                        hold attack
                        wait 20 ticks
                        release attack
                    else:
                        send "not stone: %block below player%"

                every 5 seconds:
                    if health of player is less than 6:
                        make player say "low hp!"
                """);
        assertEquals(List.of(), script.errors());
        assertEquals(2, script.triggers().size());
        Trigger first = script.triggers().get(0);
        assertEquals(new Event.KeyPress("key.keyboard.r"), first.event());
        assertEquals(1, first.line());
        assertEquals("test.ms", first.file());
        assertEquals(1, first.body().statements().size());
        IfChain chain = assertInstanceOf(IfChain.class, first.body().statements().get(0));
        assertEquals(2, chain.line());
        assertEquals(1, chain.branches().size());
        assertEquals(4, chain.branches().get(0).block().statements().size());
        assertEquals(1, chain.otherwise().statements().size());
        assertEquals(new Event.Periodic(100), script.triggers().get(1).event());
    }

    @Test
    void referenceScriptRunsAgainstTheFakeBridge() {
        ParsedScript script = parse("""
                on key press of "r":
                    if block below player is stone or cobblestone:
                        send "on stone, mining at y %player's y-coordinate%"
                        hold attack
                        wait 20 ticks
                        release attack
                    else:
                        send "not stone: %block below player%"
                """);
        game.setBlock(0, -1, 0, "minecraft:cobblestone");
        game.y = 64;
        run(script.triggers().get(0), Map.of());
        assertEquals(List.of("on stone, mining at y 64"), game.messages);
        assertEquals(List.of("attack=true", "wait:20", "attack=false"), game.calls);
        game.setBlock(0, -1, 0, "minecraft:dirt");
        run(script.triggers().get(0), Map.of());
        assertEquals("not stone: dirt", game.messages.getLast());
    }

    @Test
    void elseIfChains() {
        ParsedScript script = parse("""
                on chat:
                    if message is "a":
                        send "A"
                    else if message is "b":
                        send "B"
                    else if message is "c":
                        send "C"
                    else:
                        send "other"
                    send "done"
                """);
        assertEquals(List.of(), script.errors());
        IfChain chain = assertInstanceOf(IfChain.class, script.triggers().get(0).body().statements().get(0));
        assertEquals(3, chain.branches().size());
        assertEquals(2, script.triggers().get(0).body().statements().size());
        run(script.triggers().get(0), Map.of("message", "b"));
        assertEquals(List.of("B", "done"), game.messages);
    }

    @Test
    void ifWithoutElseAndNestedIfs() {
        ParsedScript script = parse("""
                every tick:
                    if player is sneaking:
                        if key "w" is held:
                            send "sneak walk"
                    send "tick"
                """);
        assertEquals(List.of(), script.errors());
        game.sneaking = true;
        game.keysDown.add("key.keyboard.w");
        run(script.triggers().get(0), Map.of());
        assertEquals(List.of("sneak walk", "tick"), game.messages);
    }

    @Test
    void errorsAreCollectedWithLinesAndOtherTriggersSurvive() {
        ParsedScript script = parse("""
                on load:
                    send "ok"

                on chat:
                    hold attak
                    send "never"

                every 2 seconds:
                    send "still here"
                """);
        assertEquals(List.of("test.ms:5: unknown effect \"hold attak\""), script.errors().stream().map(Object::toString).toList());
        assertEquals(2, script.triggers().size());
        assertEquals(new Event.Load(), script.triggers().get(0).event());
        assertEquals(new Event.Periodic(40), script.triggers().get(1).event());
    }

    @Test
    void specificErrorMessages() {
        assertEquals(List.of("test.ms:1: unknown event \"on sunrise\""), errors("on sunrise:\n    stop\n"));
        assertEquals(List.of("test.ms:2: unknown condition \"player is nonsense\""), errors("on load:\n    if player is nonsense:\n        stop\n"));
        assertEquals(List.of("test.ms:2: \"else\" without a matching \"if\""), errors("on load:\n    else:\n        stop\n"));
        assertEquals(List.of("test.ms:3: \"else\" without a matching \"if\""), errors("on load:\n    stop\n    else if player is sneaking:\n        stop\n"));
        assertEquals(List.of("test.ms:2: \"message\" is only available inside \"on chat\", \"on chat send\" and \"on command send\""), errors("on load:\n    send message\n"));
        assertEquals(List.of("test.ms:2: unknown section \"until true\""), errors("on load:\n    until true:\n        stop\n"));
        assertEquals(List.of("test.ms:2: unterminated string"), errors("on load:\n    send \"oops\n"));
        assertEquals(List.of("test.ms:1: expected an event section"), errors("send \"x\"\n"));
        assertEquals(List.of("test.ms:1: unknown key \"banana\""), errors("on key press of \"banana\":\n    stop\n"));
        assertEquals(List.of("test.ms:2: unknown expression \"the dragon\" in string"), errors("on load:\n    send \"%the dragon%\"\n"));
    }

    @Test
    void lexerErrorsFlowThrough() {
        ParsedScript script = parse("on load:\n send \"a\"\n  send \"b\"\n");
        assertEquals(List.of("test.ms:3: unexpected indentation"), script.errors().stream().map(Object::toString).toList());
        assertTrue(script.triggers().isEmpty());
    }

    private List<String> errors(String source) {
        return parse(source).errors().stream().map(Object::toString).toList();
    }

    @Test
    void unterminatedPercentInStringReportsUsefulError() {
        List<String> errors = errors("on load:\n    send \"unclosed %player\"\n");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).endsWith("unterminated % in string"));
    }
}
