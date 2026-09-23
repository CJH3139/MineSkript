package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffExitLoop implements Statement {
    private final int line;

    private EffExitLoop(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("exit loop is only available inside a loop");
            }
            return Optional.of(new EffExitLoop(scope.line()));
        }, "(exit|stop) [(the|this)] loop");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return Flow.EXIT_LOOP;
    }
}
