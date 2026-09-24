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

@Name("Execute Command")
@Description({"Sends a command to the server as if you typed it. A leading slash is optional and is removed, so \"/spawn\" and \"spawn\" are the same. Also written send command, command, or make player execute.",
        "The command goes to the server, so it only does what the server allows you to do. Only a single text value is accepted, not a list. Needs a world."})
@Examples({"on key press of \"h\":",
        "\texecute command \"/home\"",
        "",
        "on world join:",
        "\twait 2 seconds",
        "\tmake player execute command \"/warp shop\""})
@Since("1.0.0-alpha")
public final class EffCommand implements Statement {
    private final int line;
    private final Expression command;

    private EffCommand(int line, Expression command) {
        this.line = line;
        this.command = command;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new EffCommand(scope.line(), match.slot(0))),
                "[execute] [the] command %string%",
                "send [the] command %string%",
                "(make|let) [the] (player|me|myself) execute [[the] command] %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        String text = Converters.toText(command.evaluate(context), context).trim();
        if (text.startsWith("/")) {
            text = text.substring(1);
        }
        context.world().sendCommand(text);
        return Flow.CONTINUE;
    }
}
