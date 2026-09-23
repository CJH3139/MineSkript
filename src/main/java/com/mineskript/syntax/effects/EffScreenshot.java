package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffScreenshot implements Statement {
    private final int line;

    private EffScreenshot(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffScreenshot(scope.line())),
                "take [a] screenshot");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.game().takeScreenshot();
        return Flow.CONTINUE;
    }
}
