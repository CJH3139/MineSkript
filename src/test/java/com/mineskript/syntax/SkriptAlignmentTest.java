package com.mineskript.syntax;

import static com.mineskript.syntax.SyntaxTestSupport.effect;
import static com.mineskript.syntax.SyntaxTestSupport.event;
import static com.mineskript.syntax.SyntaxTestSupport.parser;
import static com.mineskript.syntax.SyntaxTestSupport.scope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.script.EventDispatcher;
import com.mineskript.script.ScriptRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkriptAlignmentTest {
    private final ScriptRunner runner = new ScriptRunner();
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry scripts = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(scripts, game, new Interpreter(10_000),
            new Scheduler(), new Variables(), () -> {
            });

    private void load(String source) {
        scripts.replace(List.of(new Parser(DefaultSyntax.registry()).parse("t.ms", source)));
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    private static String effectClass(String line) {
        return effect(line, new Event.Load()).getClass().getSimpleName();
    }

    private static String expressionClass(String text, SkType type) {
        return parser().parse(text, type, scope(new Event.Load())).orElseThrow().getClass().getSimpleName();
    }

    @Test
    void sendTitleAndActionBarWinOverSendMessage() {
        assertEquals("EffSendTitle", effectClass("send title \"hi\""));
        assertEquals("EffSendTitle", effectClass("send subtitle \"hi\" to player"));
        assertEquals("EffActionBar", effectClass("send action bar \"hi\""));
        assertEquals("EffActionBar", effectClass("send the actionbar with text \"hi\" to me"));
        assertEquals("EffSend", effectClass("send \"title\""));
        assertEquals("EffSend", effectClass("send title of the screen"));
        assertEquals("EffSend", effectClass("send message \"action bar\""));
    }

    @Test
    void sendTitleWithSubtitleAndTimes() {
        runner.run("""
                on load:
                    send title "a" with subtitle "b" to player for 5 seconds with fade in 1 second \
                        and fade out 2 seconds
                    send title "c"
                    send subtitle "d" to me
                    send title "e" for 1 second with fade-out 3 ticks
                    send action bar "f" to player
                    send the actionbar with text "g"
                    show title "h"
                    show action bar "i"
                """);
        assertEquals(List.of("sendTitle:a|b|20|100|40", "sendTitle:c|-|-1|-1|-1", "sendTitle:-|d|-1|-1|-1",
                "sendTitle:e|-|-1|20|3", "actionBar:f", "actionBar:g", "title:h", "actionBar:i"), runner.game.calls);
    }

    @Test
    void sendTitleOnlyGoesToTheLocalPlayer() {
        assertEquals(1, runner.errorsOf("on load:\n    send title \"a\" to nearest player\n").size());
    }

    @Test
    void textExpressionsParseAsTheirSkriptElements() {
        assertEquals("ExprJoinSplit", expressionClass("join {_l::*} with \", \"", SkType.TEXT));
        assertEquals("ExprJoinSplit", expressionClass("split \"a\" at \",\" with case sensitivity", SkType.TEXT));
        assertEquals("ExprJoin", expressionClass("\"a\" joined with \"b\"", SkType.TEXT));
        assertEquals("ExprStringCase", expressionClass("\"abc\" in upper case", SkType.TEXT));
        assertEquals("ExprSubstring", expressionClass("the 3 first characters of \"abc\"", SkType.TEXT));
        assertEquals("ExprReplace", expressionClass("\"a\" with \"b\" replaced with \"c\"", SkType.TEXT));
        assertEquals("EffReplace", effectClass("replace \"a\" with \"b\" in {_x}"));
        assertEquals("EffReplace", effectClass("replace the first \"a\" in {_x} with \"b\" with case sensitivity"));
    }

    @Test
    void aFunctionCallWinsOverTheJoinExpression() {
        runner.run("""
                function join(t: text) :: text:
                    return "function"

                on load:
                    send join("x")
                    send join "x" and "y" with "-"
                    send concat("a", "b")
                """);
        assertEquals(List.of("function", "x-y", "ab"), runner.game.messages);
    }

    @Test
    void isInStillWinsOverInUpperCase() {
        runner.run("""
                on load:
                    if "a" is in "A", "b":
                        send "found"
                    send "abc" in upper case
                """);
        assertEquals(List.of("found", "ABC"), runner.game.messages);
    }

    @Test
    void replaceNeedsSomethingThatCanBeChanged() {
        List<String> errors = runner.errorsOf("on load:\n    replace \"a\" in \"abc\" with \"b\"\n");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("cannot have parts replaced"), errors.get(0));
        assertEquals(List.of(), runner.errorsOf("on chat send:\n    replace \"a\" with \"b\" in the message\n"));
    }

    @Test
    void replaceChangesTheMessageInPlace() {
        load("on chat send:\n    replace all \"NOOB\" with \"friend\" in the message\n");
        assertTrue(dispatcher.onChatSend("hi noob"));
        assertEquals("hi friend", dispatcher.modifyChatSend("hi noob"));
    }

    @Test
    void damageAndPastHealthInHealthEvents() {
        load("""
                on damage:
                    send "damage %damage% from %past health% to %health%"
                on heal:
                    send "heal from %past health% to %player's health%"
                on health change:
                    send "change from %old health of player%"
                """);
        ticks(1);
        game.health = 14;
        ticks(1);
        game.health = 16;
        ticks(1);
        assertEquals(List.of("change from 20", "damage 6 from 20 to 14", "change from 14", "heal from 14 to 16"),
                game.messages);
    }

    @Test
    void pastHeldItemInHeldItemChange() {
        load("""
                on held item change:
                    send "%past held item% to %held item%, %former tool%, %tool before the event%"
                """);
        game.setSlot(0, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        game.setSlot(1, new ItemValue("minecraft:dirt", "dirt", 1, 0, 0));
        ticks(1);
        game.selected = 1;
        ticks(1);
        assertEquals(List.of("stone to dirt, stone, stone"), game.messages);
    }

    @Test
    void fallDistanceInLandIsTheDistanceFallen() {
        load("on land:\n    send \"%fall distance% %event-fall distance% %player's fallen height%\"\n");
        ticks(1);
        game.onGround = false;
        game.fallDistance = 4;
        ticks(1);
        game.onGround = true;
        game.fallDistance = 0;
        ticks(1);
        assertEquals(List.of("4 4 4"), game.messages);
    }

    @Test
    void fallDistanceElsewhereIsTheLiveValue() {
        runner.game.fallDistance = 2.5;
        runner.run("on load:\n    send \"%fall distance% %fall distance of player%\"\n");
        assertEquals(List.of("2.5 2.5"), runner.game.messages);
    }

    @Test
    void eventOnlyExpressionsAreRefusedElsewhere() {
        assertEquals(1, runner.errorsOf("on key press of \"g\":\n    send \"%past health%\"\n").size());
        assertEquals(1, runner.errorsOf("on damage:\n    send \"%past held item%\"\n").size());
        assertEquals(1, runner.errorsOf("on heal:\n    if damage is greater than 2:\n        stop\n").size());
        assertEquals(List.of(), runner.errorsOf("on damage:\n    send \"%damage% %event-damage% %past health%\"\n"));
        assertEquals(SkType.NUMBER, parser().parse("damage", SkType.OBJECT, scope(event("on damage")))
                .orElseThrow().type());
    }

    @Test
    void healthCanBeWrittenWithoutOfPlayer() {
        runner.game.health = 12;
        runner.run("on load:\n    send \"%health% %health of player% %player's health%\"\n");
        assertEquals(List.of("12 12 12"), runner.game.messages);
    }
}
