package com.mineskript.client.world.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Block At Location")
@Description({"The block at a location, such as location(10, 64, -3), a saved {home} or 2 below player. Decimal coordinates are fine: the block containing that point is used. Returns a block, printed as its plain name such as grass_block, and air for empty space or unloaded areas. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Your game only knows the dimension you are in, so a location in another dimension has no block: the result is none."})
@Examples({
        "on key press of \"b\":",
        "\tsend \"spawn block: %block at location(0, 64, 0)%\"",
        "",
        "on key press of \"b\":",
        "\tif block at 1 below player is diamond_ore:",
        "\t\tsend \"diamonds underfoot\""
})
@Since("1.0.0-alpha.2, 1.0.0-alpha.10 (locations)")
public final class ExprBlockAt implements Expression {
    private final Expression target;

    private ExprBlockAt(Expression target) {
        this.target = target;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.BLOCK, Tier.COMBINED, Priority.after(Priority.SIMPLE), ExprBlockAt::create,
                "[the] block at %location%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprBlockAt(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.BLOCK;
    }

    @Override
    public Object evaluate(Context context) {
        Location location = Locations.read(target, context);
        if (!Locations.isHere(location, context)) {
            return None.NONE;
        }
        return new BlockValue(context.world().blockAt(location.x(), location.y(), location.z()), location);
    }
}
