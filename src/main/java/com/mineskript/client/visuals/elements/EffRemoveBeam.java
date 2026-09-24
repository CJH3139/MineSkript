package com.mineskript.client.visuals.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Remove Beam")
@Description("Removes the beam shown at the block of a location (decimal coordinates are rounded down to the block), or every beam at once. Removing a beam that is not there, or at a location in another dimension, does nothing. Beams are also removed by themselves when you leave the world or change dimension, and when the script that made them is reloaded.")
@Examples({"on key press of \"b\":",
        "\tremove beam at location(100, 64, -200)",
        "",
        "on key press of \"v\":",
        "\tremove all beams"})
@Since("1.0.0-alpha.9, 1.0.0-alpha.10 (locations)")
public final class EffRemoveBeam implements Statement {
    private final int line;
    private final Expression target;

    private EffRemoveBeam(int line, Expression target) {
        this.line = line;
        this.target = target;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffRemoveBeam::create,
                "(remove|hide) [the] beam at %location%",
                "(remove|hide) all beams");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.patternIndex() == 1) {
            return Optional.of(new EffRemoveBeam(scope.line(), null));
        }
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffRemoveBeam(scope.line(), match.slot(0)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        if (target == null) {
            context.world().removeAllBeams();
            return Flow.CONTINUE;
        }
        Location location = Locations.read(target, context);
        if (Locations.isHere(location, context)) {
            Location block = location.blockCorner();
            context.world().removeBeam((int) block.x(), (int) block.y(), (int) block.z());
        }
        return Flow.CONTINUE;
    }
}
