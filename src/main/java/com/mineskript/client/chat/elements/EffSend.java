package com.mineskript.client.chat.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Send Message")
@Description({"Shows a message in your own chat. It is never sent to the server and only you see it. Any value can be sent: numbers, items and entities are turned into text, and a list is joined like \"a, b and c\". Also written message, send message or send messages.",
        "Works even without a world, which makes it useful in on load for status messages."})
@Examples({"on key press of \"k\":",
        "\tsend \"you are at %round player's x-coordinate%, %round player's y-coordinate%, %round player's z-coordinate%\"",
        "",
        "on player join:",
        "\tsend online player names"})
@Since("1.0.0-alpha")
public final class EffSend implements Statement {
    private final int line;
    private final Expression value;

    private EffSend(int line, Expression value) {
        this.line = line;
        this.value = value;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffSend(scope.line(), match.slot(0))),
                "(message|send [(message|messages)]) %objects%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.game().showMessage(Converters.toText(value.evaluate(context), context));
        return Flow.CONTINUE;
    }
}
