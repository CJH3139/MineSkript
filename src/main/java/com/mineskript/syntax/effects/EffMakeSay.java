package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

public final class EffMakeSay implements Statement {
    private final int line;
    private final Expression text;

    private EffMakeSay(int line, Expression text) {
        this.line = line;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new EffMakeSay(scope.line(), match.slot(0))),
                "(make|force) [the] player (say|send [the] (message|messages)) %string%");
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
