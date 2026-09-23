package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

public final class EffPlaySound implements Statement {
    private final int line;
    private final Expression sound;

    private EffPlaySound(int line, Expression sound) {
        this.line = line;
        this.sound = sound;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList()
                        ? Optional.empty()
                        : Optional.of(new EffPlaySound(scope.line(), match.slot(0))),
                "play sound %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().playSound(Converters.toText(sound.evaluate(context), context));
        return Flow.CONTINUE;
    }
}
