package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

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
