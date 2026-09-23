package com.mineskript.syntax.effects;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffSetRotation implements Statement {
    private final int line;
    private final boolean yaw;
    private final Expression angle;

    private EffSetRotation(int line, boolean yaw, Expression angle) {
        this.line = line;
        this.yaw = yaw;
        this.angle = angle;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffSetRotation::create, "set [the] (yaw:yaw|pitch:pitch) to %number%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffSetRotation(scope.line(), match.has("yaw"), match.slot(0)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        double value = (Double) angle.evaluate(context);
        if (yaw) {
            game.setYaw(value);
        } else {
            game.setPitch(value);
        }
        return Flow.CONTINUE;
    }
}
