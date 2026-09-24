package com.mineskript.client.chat.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Cancel Event")
@Description({"Stops the message or command that triggered the event from being sent. Only allowed inside on chat send and on command send; anywhere else it is an error when the script loads. Written cancel event, cancel the message or cancel the command, which all do the same thing.",
        "The decision is made when the trigger first pauses or finishes, so cancel event has to run before any wait in the trigger. If several triggers handle the same message, any one of them cancelling is enough."})
@Examples({"on chat send:",
        "\tif message contains \"password\":",
        "\t\tcancel event",
        "\t\tsend \"not sending that\"",
        "",
        "on command send:",
        "\tif message starts with \"op \":",
        "\t\tcancel the command",
        "\t\tshow action bar \"blocked /op\""})
@Since("1.0.0-alpha.6")
public final class EffCancelEvent implements Statement {
    private final int line;

    private EffCancelEvent(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> {
            if (scope.event() == null || !scope.event().context().cancellable()) {
                throw new SyntaxException("only \"on chat send\" and \"on command send\" can be cancelled");
            }
            return Optional.of(new EffCancelEvent(scope.line()));
        }, "cancel [the] (event|message|command)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.cancel();
        return Flow.CONTINUE;
    }
}
