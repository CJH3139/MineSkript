package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffWait implements Statement {
    private final int line;
    private final Expression duration;

    private EffWait(int line, Expression duration) {
        this.line = line;
        this.duration = duration;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new EffWait(scope.line(), match.slot(0))),
                "(wait|halt) [for] %timespan%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        Timespan timespan = (Timespan) duration.evaluate(context);
        return new Flow.Wait(timespan.ticks());
    }
}
