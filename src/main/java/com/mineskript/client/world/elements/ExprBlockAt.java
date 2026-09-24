package com.mineskript.client.world.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Block At Position")
@Description("The block at the given x, y and z world coordinates. Decimal coordinates are fine: the block containing that point is used. Returns a block, printed as its plain name such as grass_block, and air for empty space or unloaded areas. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"b\":",
        "\tsend \"spawn block: %block at 0, 64, 0%\"",
        "",
        "on key press of \"b\":",
        "\tset {_x} to player's x-coordinate",
        "\tset {_y} to player's y-coordinate - 1",
        "\tset {_z} to player's z-coordinate",
        "\tif block at {_x}, {_y}, {_z} is diamond_ore:",
        "\t\tsend \"diamonds underfoot\""
})
@Since("1.0.0-alpha.2")
public final class ExprBlockAt implements Expression {
    private final Expression x;
    private final Expression y;
    private final Expression z;

    private ExprBlockAt(Expression x, Expression y, Expression z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.BLOCK, Tier.COMBINED, Priority.after(Priority.SIMPLE), ExprBlockAt::create,
                "[the] block at %number%, %number%, %number%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList() || match.slot(2).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprBlockAt(match.slot(0), match.slot(1), match.slot(2)));
    }

    @Override
    public SkType type() {
        return SkType.BLOCK;
    }

    @Override
    public Object evaluate(Context context) {
        return new BlockValue(context.world().blockAt(
                (Double) x.evaluate(context), (Double) y.evaluate(context), (Double) z.evaluate(context)));
    }
}
