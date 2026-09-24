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
import java.util.List;
import java.util.Optional;

@Name("Remove Client Entity")
@Description({"Removes a client entity (hologram, item display or block display) you spawned, given by its number from last spawned client entity, or every client entity at once. Removing one that is already gone does nothing.",
        "Client entities are also removed by themselves when you leave the world or change dimension, and when the script that made them is reloaded."})
@Examples({"on key press of \"g\":",
        "	if {-home hologram} is set:",
        "		remove client entity {-home hologram}",
        "		delete {-home hologram}",
        "",
        "on key press of \"k\":",
        "	remove all client entities"})
@Since("1.0.0-alpha.9")
public final class EffRemoveClientEntity implements Statement {
    private final int line;
    private final Expression handles;

    private EffRemoveClientEntity(int line, Expression handles) {
        this.line = line;
        this.handles = handles;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffRemoveClientEntity::create,
                "(remove|despawn) (client entity|client entities|hologram|holograms) %numbers%",
                "(remove|despawn) all (client entities|holograms)");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        return Optional.of(new EffRemoveClientEntity(scope.line(), match.patternIndex() == 0 ? match.slot(0) : null));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        if (handles == null) {
            context.world().removeAllClientEntities();
            return Flow.CONTINUE;
        }
        Object value = handles.evaluate(context);
        List<?> numbers = value instanceof List<?> list ? list : List.of(value);
        for (Object number : numbers) {
            context.world().removeClientEntity(ClientEntityHandles.handle(number));
        }
        return Flow.CONTINUE;
    }
}
