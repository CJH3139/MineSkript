package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffStop implements Statement {
    private final int line;

    private EffStop(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffStop(scope.line())), "(exit|stop) [trigger]");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return Flow.STOP;
    }
}
