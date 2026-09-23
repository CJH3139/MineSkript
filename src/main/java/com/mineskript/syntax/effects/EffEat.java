package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

public final class EffEat implements Statement {
    private static final int START_TICKS = 2;

    private final int line;
    private final Block steps;

    private EffEat(int line) {
        this.line = line;
        this.steps = new Block(List.of(
                new Pause(line),
                new WaitUntil(line, context -> !context.world().isUsingItem()),
                new Release(line)));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffEat(scope.line())),
                "(eat|drink|consume) [the] [(held|holding)] (item|food)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().setUseHeld(true);
        return new Flow.Enter(steps);
    }

    private record Pause(int line) implements Statement {
        @Override
        public Flow execute(Context context) {
            return new Flow.Wait(START_TICKS);
        }
    }

    private record Release(int line) implements Statement {
        @Override
        public Flow execute(Context context) {
            context.world().setUseHeld(false);
            return Flow.CONTINUE;
        }
    }
}
