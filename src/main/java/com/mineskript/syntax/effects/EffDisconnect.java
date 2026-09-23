package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffDisconnect implements Statement {
    private final int line;

    private EffDisconnect(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffDisconnect(scope.line())),
                "disconnect",
                "(leave|quit) [the] (server|world|game)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().disconnect();
        return Flow.STOP;
    }
}
