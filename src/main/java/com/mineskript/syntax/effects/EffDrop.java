package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffDrop implements Statement {
    private final int line;
    private final boolean wholeStack;

    private EffDrop(int line, boolean wholeStack) {
        this.line = line;
        this.wholeStack = wholeStack;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffDrop(scope.line(), match.patternIndex() == 1)),
                "drop [the] item",
                "drop [the] (stack|whole stack)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().dropItem(wholeStack);
        return Flow.CONTINUE;
    }
}
