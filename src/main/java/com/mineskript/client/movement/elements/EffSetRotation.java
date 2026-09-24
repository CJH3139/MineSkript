package com.mineskript.client.movement.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Set Yaw / Pitch")
@Description({"Sets the direction you face. Yaw is the horizontal angle in degrees: 0 faces south, 90 west, 180 north and -90 east. Pitch is the vertical angle: 0 is level, -90 straight up and 90 straight down.",
        "Needs a world."})
@Examples({"on key press of \"n\":",
        "	set yaw to 180",
        "	set pitch to 0",
        "	send \"facing north\""})
@Since("1.0.0-alpha.2")
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
