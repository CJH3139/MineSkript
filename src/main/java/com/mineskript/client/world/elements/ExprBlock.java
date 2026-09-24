package com.mineskript.client.world.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Map;
import java.util.Optional;

@Name("Block Relative To Player")
@Description({
        "The block next to the block your feet are in, in a given direction: above (also over, up), below (also under, beneath, down), north, south, east or west, or at for the block your feet are in. An optional number of blocks sets how far to look (default 1) and is rounded to a whole number; it is ignored for at. Returns a block, which prints as its plain name such as stone or oak_leaves, and air when there is nothing there. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Positions come from the block that contains your feet. While standing on a slab, carpet or snow layer, block at player is that block and block below player is the one under it. North is -z, south is +z, east is +x and west is -x."
})
@Examples({
        "on key press of \"r\":",
        "\tif block below player is stone:",
        "\t\tsend \"standing on stone\"",
        "",
        "on key press of \"r\":",
        "\tsend \"2 blocks up: %block 2 above player%, north: %block north of player%\""
})
@Since("1.0.0-alpha")
public final class ExprBlock implements Expression {
    private static final String PATTERN = "[the] block [%-number% [(block|blocks)]] "
            + "(above:above|above:over|above:up|below:below|below:under|below:beneath|below:down|north:north|south:south|east:east|west:west|at:at) [of] [the] [(player|me|myself)]";
    private static final Map<String, int[]> DIRECTIONS = Map.of(
            "above", new int[] {0, 1, 0},
            "below", new int[] {0, -1, 0},
            "north", new int[] {0, 0, -1},
            "south", new int[] {0, 0, 1},
            "east", new int[] {1, 0, 0},
            "west", new int[] {-1, 0, 0},
            "at", new int[] {0, 0, 0});

    private final Expression distance;
    private final int[] direction;

    private ExprBlock(Expression distance, int[] direction) {
        this.distance = distance;
        this.direction = direction;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.BLOCK, Tier.COMBINED, ExprBlock::create, PATTERN);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        String tag = DIRECTIONS.keySet().stream().filter(match::has).findFirst().orElse("at");
        Expression distance = match.slot(0);
        if (distance != null && distance.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprBlock(distance, DIRECTIONS.get(tag)));
    }

    @Override
    public SkType type() {
        return SkType.BLOCK;
    }

    @Override
    public Object evaluate(Context context) {
        int steps = distance == null ? 1 : (int) Math.round((Double) distance.evaluate(context));
        if (direction[0] == 0 && direction[1] == 0 && direction[2] == 0) {
            steps = 0;
        }
        GameBridge world = context.world();
        int dx = direction[0] * steps;
        int dy = direction[1] * steps;
        int dz = direction[2] * steps;
        Location feet = new Location(world.playerX(), world.playerY(), world.playerZ(), world.dimension());
        return new BlockValue(world.blockIdAt(dx, dy, dz), feet.blockCorner().offset(dx, dy, dz));
    }
}
