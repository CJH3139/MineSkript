package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

public final class EffClipboard implements Statement {
    private final int line;
    private final Expression text;

    private EffClipboard(int line, Expression text) {
        this.line = line;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffClipboard(scope.line(), match.slot(0))),
                "copy %objects% to [the] clipboard");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.game().copyToClipboard(Converters.toText(text.evaluate(context), context));
        return Flow.CONTINUE;
    }
}
