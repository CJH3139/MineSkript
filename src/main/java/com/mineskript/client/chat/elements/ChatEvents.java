package com.mineskript.client.chat.elements;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class ChatEvents {
    private ChatEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEvent("Chat", (match, scope) -> Optional.of(new Event.Chat()), "on chat")
                .values("message")
                .description("Fires for every message that appears in your chat from the server: player chat and system messages. Action bar (overlay) messages are not included. The text is available as message, with colours and formatting removed. Nothing fires while no world is loaded.",
                        "For player chat the text is the message as displayed, which normally includes the sender name, so match with contains rather than is.")
                .examples("on chat:",
                        "\tif message contains \"has joined\":",
                        "\t\tsend \"someone joined\"",
                        "",
                        "on chat:",
                        "\tif message contains \"your name\":",
                        "\t\tplay sound \"minecraft:block.note_block.pling\"")
                .since("1.0.0-alpha");
        registry.addEvent("Chat Send", (match, scope) -> Optional.of(new Event.ChatSend()), "on chat send")
                .values("message")
                .cancellable()
                .description("Fires when you send a chat message (not a command), before it leaves the client. The text is available as message, and cancel event stops the message from being sent at all. Lines starting with the effect command prefix (? by default) run as effect commands and do not fire this event.",
                        "cancel event only works if it runs before the first wait in the trigger; after a wait the message has already gone. Chat you send from inside this event with make player say does not fire it again.")
                .examples("on chat send:",
                        "\tif message contains \"password\":",
                        "\t\tcancel event",
                        "\t\tsend \"blocked a message that looked private\"",
                        "",
                        "on chat send:",
                        "\tset {-last sent} to message")
                .since("1.0.0-alpha.2");
        registry.addEvent("Command Send", (match, scope) -> Optional.of(new Event.CommandSend()), "on command send")
                .values("message")
                .cancellable()
                .description("Fires when you send a command, before it leaves the client. The command text is available as message, without the leading slash, and cancel event stops the command from being sent.",
                        "As with chat send, cancel event must run before any wait to have an effect.")
                .examples("on command send:",
                        "\tif message starts with \"gamemode\":",
                        "\t\tsend \"gamemode change requested\"",
                        "",
                        "on command send:",
                        "\tif message is \"spawn\":",
                        "\t\tcancel event",
                        "\t\tsend \"use /home instead\"")
                .since("1.0.0-alpha.2");
    }
}
