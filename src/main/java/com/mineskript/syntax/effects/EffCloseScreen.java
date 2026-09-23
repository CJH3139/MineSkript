package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffCloseScreen implements Statement {
    private final int line;

    private EffCloseScreen(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffCloseScreen(scope.line())),
                "close [the] screen",
                "close [the] (inventory|gui)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().closeScreen();
        return Flow.CONTINUE;
    }
}
