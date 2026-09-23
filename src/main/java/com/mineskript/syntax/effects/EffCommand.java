package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

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
                "(make|let) [the] player execute [[the] command] %string%");
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
