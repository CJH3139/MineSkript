package com.mineskript.client.world.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Dimension")
@Description({
        "The dimension you are in as a namespaced id text: minecraft:overworld, minecraft:the_nether or minecraft:the_end (or a modded or server-defined id). Needs a world: outside a world the line stops with a \"no world\" error.",
        "Skript calls this the world: world, world of player and player's world give the same id. The dimension or world of a location gives the dimension that location is in, such as world of {home}."
})
@Examples({
        "on dimension change:",
        "	send \"now in %dimension%\"",
        "",
        "on key press of \"d\":",
        "	if dimension is \"minecraft:the_nether\":",
        "		send \"in the nether\"",
        "",
        "on key press of \"h\":",
        "	if world of {home} is not world of player:",
        "		send \"your home is in %world of {home}%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprDimension implements Expression {
    private static final String NAMES = "(dimension|world)";

    private final Expression location;

    private ExprDimension(Expression location) {
        this.location = location;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> create(match.slot(0)),
                "[the] " + NAMES + " [of %locations%]",
                "%locations%'s " + NAMES);
    }

    private static Optional<Expression> create(Expression location) {
        if (location != null && location.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprDimension(location));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        if (location == null) {
            return context.world().dimension();
        }
        return Locations.read(location, context).dimension();
    }
}
