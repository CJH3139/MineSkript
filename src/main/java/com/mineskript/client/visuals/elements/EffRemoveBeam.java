package com.mineskript.client.visuals.elements;

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

@Name("Remove Beam")
@Description("Removes the beam shown at the block x, y, z (decimal coordinates are rounded down to the block), or every beam at once. Removing a beam that is not there does nothing. Beams are also removed by themselves when you leave the world or change dimension, and when the script that made them is reloaded.")
@Examples({"on key press of \"b\":",
        "\tremove beam at 100, 64, -200",
        "",
        "on key press of \"v\":",
        "\tremove all beams"})
@Since("1.0.0-alpha.9")
public final class EffRemoveBeam implements Statement {
    private final int line;
    private final Expression x;
    private final Expression y;
    private final Expression z;

    private EffRemoveBeam(int line, Expression x, Expression y, Expression z) {
        this.line = line;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffRemoveBeam::create,
                "(remove|hide) [the] beam at %number%, %number%, %number%",
                "(remove|hide) all beams");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.patternIndex() == 1) {
            return Optional.of(new EffRemoveBeam(scope.line(), null, null, null));
        }
        for (int i = 0; i < 3; i++) {
            if (match.slot(i).isList()) {
                return Optional.empty();
            }
        }
        return Optional.of(new EffRemoveBeam(scope.line(), match.slot(0), match.slot(1), match.slot(2)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        if (x == null) {
            context.world().removeAllBeams();
            return Flow.CONTINUE;
        }
        context.world().removeBeam(EffShowBeam.block(x, context), EffShowBeam.block(y, context),
                EffShowBeam.block(z, context));
        return Flow.CONTINUE;
    }
}
