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

@Name("Move Client Entity")
@Description("Moves a client entity (a hologram, item display or block display you spawned) to the x, y, z world coordinates straight away. The entity is given by its number from last spawned client entity. Does nothing if it no longer exists, for example after you changed worlds.")
@Examples({"every 5 ticks:",
        "\tif {-marker} is set:",
        "\t\tmove client entity {-marker} to player's x-coordinate, player's y-coordinate + 2.5, player's z-coordinate"})
@Since("1.0.0-alpha.9")
public final class EffMoveClientEntity implements Statement {
    private final int line;
    private final Expression handle;
    private final Expression x;
    private final Expression y;
    private final Expression z;

    private EffMoveClientEntity(int line, Expression handle, Expression x, Expression y, Expression z) {
        this.line = line;
        this.handle = handle;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffMoveClientEntity::create,
                "(move|teleport) (client entity|hologram) %number% to %number%, %number%, %number%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        for (int i = 0; i < 4; i++) {
            if (match.slot(i).isList()) {
                return Optional.empty();
            }
        }
        return Optional.of(new EffMoveClientEntity(scope.line(), match.slot(0), match.slot(1), match.slot(2),
                match.slot(3)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        int id = ClientEntityHandles.handle(handle.evaluate(context));
        context.world().moveClientEntity(id, (Double) x.evaluate(context), (Double) y.evaluate(context),
                (Double) z.evaluate(context));
        return Flow.CONTINUE;
    }
}
