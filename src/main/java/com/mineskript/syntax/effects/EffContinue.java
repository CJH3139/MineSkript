package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffContinue implements Statement {
    private final int line;

    private EffContinue(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("continue is only available inside a loop");
            }
            return Optional.of(new EffContinue(scope.line()));
        }, "continue [(this loop|[the] [current] loop)]");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return Flow.NEXT_ITERATION;
    }
}
