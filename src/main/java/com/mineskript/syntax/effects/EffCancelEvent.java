package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffCancelEvent implements Statement {
    private final int line;

    private EffCancelEvent(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> {
            if (!(scope.event() instanceof Event.ChatSend) && !(scope.event() instanceof Event.CommandSend)) {
                throw new SyntaxException("only \"on chat send\" and \"on command send\" can be cancelled");
            }
            return Optional.of(new EffCancelEvent(scope.line()));
        }, "cancel [the] (event|message|command)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.cancel();
        return Flow.CONTINUE;
    }
}
