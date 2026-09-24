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

@Name("Make Player Say")
@Description({"Sends a chat message to the server as you, visible to everyone else. Written make player say, make me say, or force the player send message.",
        "Unlike Send, other players see this. Use Execute Command for commands. Needs a world."})
@Examples({"on world join:",
        "\twait 3 seconds",
        "\tmake player say \"hello everyone\""})
@Since("1.0.0-alpha.3")
public final class EffMakeSay implements Statement {
    private final int line;
    private final Expression text;

    private EffMakeSay(int line, Expression text) {
        this.line = line;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new EffMakeSay(scope.line(), match.slot(0))),
                "(make|force) [the] (player|me|myself) (say|send [the] (message|messages)) %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().sendChat(Converters.toText(text.evaluate(context), context));
        return Flow.CONTINUE;
    }
}
