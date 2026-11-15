package com.mineskript.client.chat.elements;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class ChatEvents {
    private ChatEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEvent("Chat", (match, scope) -> Optional.of(new Event.Chat()), "on chat")
                .values("message", "sender")
                .description("Fires for every message that appears in your chat from the server: player chat and system messages. Action bar (overlay) messages are not included. The text is available as message, with colours and formatting removed. Nothing fires while no world is loaded.",
                        "For player chat the text is the message as displayed, which normally includes the sender name, so match with contains rather than is.",
                        "event-sender is the name of the player who sent the message. It is only set for player chat the server sends as player chat; system messages, and the chat of many servers that format chat themselves, have no sender, so check if event-sender is set.")
                .examples("on chat:",
                        "	if message contains \"has joined\":",
                        "		send \"someone joined\"",
                        "",
                        "on chat:",
                        "	if message contains \"your name\":",
                        "		play sound \"minecraft:block.note_block.pling\"",
                        "",
                        "on chat:",
                        "	if event-sender is set:",
                        "		send \"%event-sender% said something\"")
                .since("1.0.0-alpha", "1.0.0-alpha.18");
        registry.addEvent("Chat Send", (match, scope) -> Optional.of(new Event.ChatSend()), "on chat send")
                .values("message")
                .cancellable()
                .description("Fires when you send a chat message (not a command), before it leaves the client. The text is available as message, and cancel event stops the message from being sent at all. Lines starting with the effect command prefix (? by default) run as effect commands and do not fire this event.",
                        "cancel event only works if it runs before the first wait in the trigger; after a wait the message has already gone. Chat you send from inside this event with make player say does not fire it again.")
                .examples("on chat send:",
                        "	if message contains \"password\":",
                        "		cancel event",
                        "		send \"blocked a message that looked private\"",
                        "",
                        "on chat send:",
                        "	set {-last sent} to message")
                .since("1.0.0-alpha.2");
        registry.addEvent("Command Send", (match, scope) -> Optional.of(new Event.CommandSend()), "on command send")
                .values("message")
                .cancellable()
                .description("Fires when you send a command, before it leaves the client. The command text is available as message, without the leading slash, and cancel event stops the command from being sent.",
                        "As with chat send, cancel event must run before any wait to have an effect.")
                .examples("on command send:",
                        "	if message starts with \"gamemode\":",
                        "		send \"gamemode change requested\"",
                        "",
                        "on command send:",
                        "	if message is \"spawn\":",
                        "		cancel event",
                        "		send \"use /home instead\"")
                .since("1.0.0-alpha.2");
    }
}
