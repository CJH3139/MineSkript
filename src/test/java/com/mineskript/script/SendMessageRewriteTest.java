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

class SendMessageRewriteTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), new Variables(), () -> {
    });

    private List<String> load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        registry.replace(List.of(script));
        dispatcher.tick();
        return script.errors().stream().map(Object::toString).toList();
    }

    private String sendChat(String message) {
        return dispatcher.onChatSend(message) ? dispatcher.modifyChatSend(message) : null;
    }

    private String sendCommand(String command) {
        return dispatcher.onCommandSend(command) ? dispatcher.modifyCommandSend(command) : null;
    }

    @Test
    void settingTheMessageReplacesTheOutgoingChat() {
        assertEquals(List.of(), load("on chat send:\n    set message to \"[me] %message%\"\n    send message\n"));
        assertEquals("[me] hello", sendChat("hello"));
        assertEquals(List.of("[me] hello"), game.messages);
    }

    @Test
    void settingTheMessageReplacesTheOutgoingCommand() {
        load("on command send:\n    if message is \"h\":\n        set message to \"home\"\n");
        assertEquals("home", sendCommand("h"));
        assertEquals("spawn", sendCommand("spawn"));
    }

    @Test
    void untouchedMessagesPassThroughUnchanged() {
        load("on chat send:\n    send \"saw %message%\"\n");
        assertEquals("hi", sendChat("hi"));
        assertEquals("other", dispatcher.modifyChatSend("other"));
    }

    @Test
    void laterTriggersSeeTheEarlierReplacement() {
        load("on chat send:\n    set message to \"%message%!\"\non chat send:\n    set message to \"%message%?\"\n");
        assertEquals("hi!?", sendChat("hi"));
    }

    @Test
    void aCancelledMessageIsNotRewritten() {
        load("on chat send:\n    set message to \"changed\"\n    cancel event\n");
        assertFalse(dispatcher.onChatSend("hi"));
        assertEquals("hi", dispatcher.modifyChatSend("hi"));
    }

    @Test
    void aReplacementOnlyAppliesToTheMessageItWasMadeFor() {
        load("on chat send:\n    set message to \"changed\"\n");
        assertTrue(dispatcher.onChatSend("first"));
        assertEquals("second", dispatcher.modifyChatSend("second"));
        assertTrue(dispatcher.onChatSend("third"));
        assertEquals("changed", dispatcher.modifyChatSend("third"));
        assertEquals("third", dispatcher.modifyChatSend("third"));
    }

    @Test
    void aChangeAfterAWaitIsTooLate() {
        load("on chat send:\n    wait 1 tick\n    set message to \"late\"\n");
        assertEquals("hi", sendChat("hi"));
    }

    @Test
    void theReceivedMessageCannotBeChanged() {
        assertEquals(List.of("t.ms:2: the received message cannot be changed"), load("on chat:\n    set message to \"x\"\n"));
        assertEquals(List.of("t.ms:2: the message can only be set, not deleted"), load("on chat send:\n    delete message\n"));
        assertEquals(List.of("t.ms:2: the message can only be set, not added to"), load("on command send:\n    add \"x\" to message\n"));
    }
}
