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

@Name("Move Client Entity")
@Description({"Moves a client entity (a hologram, item display or block display you spawned) to a location straight away. The entity is given by its number from last spawned client entity. Does nothing if it no longer exists, for example after you changed worlds.",
        "Client entities only exist in the dimension you are in, so a location in another dimension stops the line with an error."})
@Examples({"every 5 ticks:",
        "\tif {-marker} is set:",
        "\t\tmove client entity {-marker} to 2.5 above player"})
@Since("1.0.0-alpha.9, 1.0.0-alpha.10 (locations)")
public final class EffMoveClientEntity implements Statement {
    private final int line;
    private final Expression handle;
    private final Expression target;

    private EffMoveClientEntity(int line, Expression handle, Expression target) {
        this.line = line;
        this.handle = handle;
        this.target = target;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffMoveClientEntity::create,
                "(move|teleport) (client entity|hologram) %number% to %location%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffMoveClientEntity(scope.line(), match.slot(0), match.slot(1)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        int id = ClientEntityHandles.handle(handle.evaluate(context));
        Location location = Locations.here(target, context);
        context.world().moveClientEntity(id, location.x(), location.y(), location.z());
        return Flow.CONTINUE;
    }
}
