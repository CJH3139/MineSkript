package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;

class SendMessageEventsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
    });

    private List<String> load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        registry.replace(List.of(script));
        return script.errors().stream().map(Object::toString).toList();
    }

    @Test
    void chatSendCarriesTheOutgoingLine() {
        load("on chat send:\n    send \"sent %message%\"\n");
        dispatcher.tick();
        dispatcher.onChatSend("hello world");
        assertEquals(List.of("sent hello world"), game.messages);
    }

    @Test
    void commandSendCarriesTheCommandWithoutItsSlash() {
        load("on command send:\n    send \"ran %message%\"\n");
        dispatcher.tick();
        dispatcher.onCommandSend("spawn");
        assertEquals(List.of("ran spawn"), game.messages);
    }

    @Test
    void theTwoSendEventsDoNotCrossTalkAndDoNotFireOnReceivedChat() {
        load("on chat send:\n    send \"out\"\non command send:\n    send \"cmd\"\non chat:\n    send \"in\"\n");
        dispatcher.tick();
        dispatcher.onChatSend("hi");
        dispatcher.onCommandSend("spawn");
        dispatcher.onChat("someone said something");
        assertEquals(List.of("out", "cmd", "in"), game.messages);
    }

    @Test
    void neitherSendEventFiresWithoutAWorld() {
        assertEquals(List.of(), load("on chat send:\n    send \"out\"\non command send:\n    send \"cmd\"\n"));
        game.hasWorld = false;
        dispatcher.tick();
        dispatcher.onChatSend("hi");
        dispatcher.onCommandSend("spawn");
        assertEquals(List.of(), game.messages);
    }

    @Test
    void conditionsAndEffectsWorkInsideTheSendEvents() {
        load("on command send:\n    if message is \"spawn\":\n        send \"going home\"\n");
        dispatcher.tick();
        dispatcher.onCommandSend("spawn");
        dispatcher.onCommandSend("tpa Alex");
        assertEquals(List.of("going home"), game.messages);
    }

    @Test
    void messageOutsideAnyChatEventIsStillAParseError() {
        List<String> errors = new Parser(DefaultSyntax.registry()).parse("t.ms", "on load:\n    send message\n")
                .errors().stream().map(Object::toString).toList();
        assertEquals(List.of("t.ms:2: \"message\" is only available inside \"on chat\", \"on chat send\" and \"on command send\""), errors);
    }

    @Test
    void chatSentFromInsideChatSendDoesNotReEnterTheEvent() {
        Loop loop = new Loop();
        loop.load("on chat send:\n    make player say \"echo %message%\"\n");
        loop.dispatcher.tick();
        loop.dispatcher.onChatSend("hi");
        assertEquals(List.of("chat:echo hi"), loop.game.calls);
    }

    @Test
    void aCommandSentFromInsideChatSendReachesTheOtherSendEvent() {
        Loop loop = new Loop();
        loop.load("on chat send:\n    execute command \"back\"\non command send:\n    make player say \"looped\"\n");
        loop.dispatcher.tick();
        loop.dispatcher.onChatSend("hi");
        assertEquals(List.of("command:back", "chat:looped"), loop.game.calls);
    }

    @Test
    void chatSendAnsweringWithACommandDeliversTheCommandSendTrigger() {
        Loop loop = new Loop();
        loop.load("on chat send:\n    execute command \"back\"\non command send:\n    send \"saw %message%\"\n");
        loop.dispatcher.tick();
        loop.dispatcher.onChatSend("hi");
        assertEquals(List.of("command:back"), loop.game.calls);
        assertEquals(List.of("saw back"), loop.game.messages);
    }

    @Test
    void theTwoHopCycleBetweenTheSendEventsStillStops() {
        Loop loop = new Loop();
        loop.load("on command send:\n    make player say \"looped\"\non chat send:\n    execute command \"back\"\n");
        loop.dispatcher.tick();
        loop.dispatcher.onCommandSend("start");
        assertEquals(List.of("chat:looped", "command:back"), loop.game.calls);
    }

    @Test
    void textConditionsWorkInsideTheSendEvents() {
        load("on command send:\n    if message contains \"spawn\":\n        send \"going home\"\n");
        dispatcher.tick();
        dispatcher.onCommandSend("spawn");
        dispatcher.onCommandSend("warp spawnpoint");
        dispatcher.onCommandSend("tpa Alex");
        assertEquals(List.of("going home", "going home"), game.messages);
    }

    private static final class Loop {
        private final FakeGameBridge game = new FakeGameBridge();
        private final ScriptRegistry registry = new ScriptRegistry();
        private final EventDispatcher dispatcher;

        private Loop() {
            GameBridge reentrant = (GameBridge) Proxy.newProxyInstance(GameBridge.class.getClassLoader(),
                    new Class<?>[] {GameBridge.class}, new Reentrant(this));
            dispatcher = new EventDispatcher(registry, reentrant, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
            });
        }

        private void load(String source) {
            registry.replace(List.of(new Parser(DefaultSyntax.registry()).parse("t.ms", source)));
        }
    }

    private static final class Reentrant implements InvocationHandler {
        private final Loop loop;

        private Reentrant(Loop loop) {
            this.loop = loop;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result;
            try {
                result = method.invoke(loop.game, args);
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
            if (method.getName().equals("sendChat")) {
                loop.dispatcher.onChatSend((String) args[0]);
            } else if (method.getName().equals("sendCommand")) {
                loop.dispatcher.onCommandSend((String) args[0]);
            }
            return result;
        }
    }
}
