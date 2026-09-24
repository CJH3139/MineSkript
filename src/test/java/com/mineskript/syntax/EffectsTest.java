package com.mineskript.syntax;

import static com.mineskript.syntax.SyntaxTestSupport.context;
import static com.mineskript.syntax.SyntaxTestSupport.effect;
import static com.mineskript.syntax.SyntaxTestSupport.holder;
import static com.mineskript.syntax.SyntaxTestSupport.scope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.Tokenizer;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EffectsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final Context context = context(game, Map.of("message", "hi"));

    private Flow run(String text) {
        Statement statement = effect(text, SyntaxTestSupport.event("on chat"));
        assertEquals(1, statement.line());
        return statement.execute(context);
    }

    private boolean parses(String text) {
        SyntaxTestSupport.SyntaxRegistryHolder holder = holder();
        return holder.registry().matchFirst(holder.registry().effects(), Tokenizer.tokenize(text), holder.parser(), scope(new Event.Load())).isPresent();
    }

    @Test
    void waitReturnsWaitFlow() {
        assertEquals(new Flow.Wait(20), run("wait 20 ticks"));
        assertEquals(new Flow.Wait(40), run("wait for 2 seconds"));
        assertEquals(new Flow.Wait(1), run("halt for a tick"));
        assertTrue(!parses("wait 1 tick or 2 ticks"));
    }

    @Test
    void sendShowsLocalMessagesFromAnyValue() {
        game.health = 6;
        assertEquals(Flow.CONTINUE, run("send \"hp %health of player%\""));
        run("message message");
        run("send message \"a\", \"b\" and \"c\"");
        run("send messages 5");
        assertEquals(List.of("hp 6", "hi", "a, b and c", "5"), game.messages);
        game.hasWorld = false;
        run("send \"still works\"");
        assertEquals("still works", game.messages.getLast());
    }

    @Test
    void makePlayerSaySendsChat() {
        run("make player say \"hello\"");
        run("force the player send message \"there\"");
        assertEquals(List.of("chat:hello", "chat:there"), game.calls);
    }

    @Test
    void commandsStripTheLeadingSlash() {
        run("execute command \"/home\"");
        run("command \"spawn\"");
        run("make player execute command \"/tp 1 2 3\"");
        run("let the player execute \"sethome\"");
        assertEquals(List.of("command:home", "command:spawn", "command:tp 1 2 3", "command:sethome"), game.calls);
    }

    @Test
    void stopReturnsStopFlow() {
        assertEquals(Flow.STOP, run("stop"));
        assertEquals(Flow.STOP, run("exit trigger"));
    }

    @Test
    void attackAndUseEffects() {
        run("click attack");
        run("press the use button");
        run("hold attack");
        run("hold the use key");
        run("release attack");
        run("release use");
        assertEquals(List.of("clickAttack", "clickUse", "attack=true", "use=true", "attack=false", "use=false"), game.calls);
    }

    @Test
    void namedKeyEffects() {
        run("press key \"w\"");
        run("click the key \"space\"");
        run("hold key \"left shift\"");
        run("release the key \"shift\"");
        assertEquals(List.of("click:key.keyboard.w", "click:key.keyboard.space", "key.keyboard.left.shift=true", "key.keyboard.left.shift=false"), game.calls);
        SyntaxException error = assertThrows(SyntaxException.class, () -> effect("press key \"banana\"", new Event.Load()));
        assertEquals("unknown key \"banana\"", error.getMessage());
    }

    @Test
    void worldEffectsFailWithoutAWorld() {
        game.hasWorld = false;
        ScriptError error = assertThrows(ScriptError.class, () -> run("click attack"));
        assertEquals("no world", error.getMessage());
        assertThrows(ScriptError.class, () -> run("make player say \"x\""));
        assertThrows(ScriptError.class, () -> run("command \"x\""));
    }

    @Test
    void unknownEffectsDoNotParse() {
        assertTrue(!parses("fly to the moon"));
        assertTrue(!parses("send"));
        assertInstanceOf(Statement.class, effect("stop", new Event.Load()));
    }
}
