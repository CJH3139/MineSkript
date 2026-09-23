package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

public final class EffStopScripts implements Statement {
    private final int line;
    private final Expression name;

    private EffStopScripts(int line, Expression name) {
        this.line = line;
        this.name = name;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffStopScripts(scope.line(), null)),
                "(stop|cancel) all [running] scripts");
        registry.addEffect((match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new EffStopScripts(scope.line(), match.slot(0))),
                "(stop|cancel) [the] script %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        if (name == null) {
            context.control().stopAll();
            return Flow.STOP;
        }
        String file = Converters.toText(name.evaluate(context), context).strip();
        if (!file.toLowerCase(Locale.ROOT).endsWith(".ms")) {
            file = file + ".ms";
        }
        context.control().stopScript(file);
        return file.equalsIgnoreCase(context.triggerFile()) ? Flow.STOP : Flow.CONTINUE;
    }
}
