package com.mineskript.client.chat.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Name("Message")
@Description({
        "The chat text of the event. In on chat it is the whole received line as shown in chat, including any sender prefix the server adds, and system messages too. In on chat send it is what you are about to send, and in on command send it is the command you typed. Also written chat message. Using it in any other event is a parse error.",
        "For commands the text does not include the leading slash.",
        "In on chat send and on command send the message can be set, which changes what is actually sent. Only a change made before the trigger's first wait counts, because the message leaves as soon as the triggers stop running. A received message in on chat cannot be changed."
})
@Examples({
        "on chat:",
        "\tif message contains \"your turn\":",
        "\t\tplay sound \"minecraft:block.note_block.pling\"",
        "",
        "on chat send:",
        "\tif message starts with \"!\":",
        "\t\tcancel event",
        "\t\tsend \"that was a local note\"",
        "",
        "on chat send:",
        "\tset message to \"%message% :)\"",
        "",
        "on command send:",
        "\tif message is \"h\":",
        "\t\tset message to \"home\""
})
@Since({"1.0.0-alpha", "1.0.0-alpha.8"})
public final class ExprMessage implements Expression, Changeable {
    private final boolean outgoing;

    private ExprMessage(boolean outgoing) {
        this.outgoing = outgoing;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> {
            Event event = scope.event();
            if (event == null || !event.context().provides("message")) {
                throw new SyntaxException("\"message\" is only available inside \"on chat\", \"on chat send\" and \"on command send\"");
            }
            // Only a message that has not been sent yet can still be changed; that is exactly when it can be cancelled.
            return Optional.of(new ExprMessage(event.context().cancellable()));
        }, "[the] [chat] message");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValue("message");
    }

    @Override
    public String changeName() {
        return outgoing ? "the message" : "the received message";
    }

    @Override
    public Set<ChangeMode> changeModes() {
        return outgoing ? EnumSet.of(ChangeMode.SET) : EnumSet.noneOf(ChangeMode.class);
    }

    @Override
    public SkType changeType(ChangeMode mode) {
        return SkType.TEXT;
    }

    @Override
    public void change(Context context, ChangeMode mode, Object value) {
        context.setEventValue("message", value);
    }
}
