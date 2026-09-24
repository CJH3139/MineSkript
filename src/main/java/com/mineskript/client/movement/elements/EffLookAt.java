package com.mineskript.client.movement.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Look At")
@Description({"Turns your camera instantly so your eyes point at the coordinates x, y, z. Add 0.5 to block coordinates to aim at a block's centre.",
        "Needs a world. The turn is a single jump, not a smooth motion."})
@Examples({"on key press of \"l\":",
        "\tlook at 0.5, 64.5, 0.5",
        "\tsend \"facing spawn\""})
@Since("1.0.0-alpha.2")
public final class EffLookAt implements Statement {
    private final int line;
    private final Expression x;
    private final Expression y;
    private final Expression z;

    private EffLookAt(int line, Expression x, Expression y, Expression z) {
        this.line = line;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffLookAt::create, "look at %number%, %number%, %number%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList() || match.slot(2).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffLookAt(scope.line(), match.slot(0), match.slot(1), match.slot(2)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().lookAt((Double) x.evaluate(context), (Double) y.evaluate(context), (Double) z.evaluate(context));
        return Flow.CONTINUE;
    }
}
