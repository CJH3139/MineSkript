package com.mineskript.client.world.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.None;
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
        "Positions come from the block that contains your feet. While standing on a slab, carpet or snow layer, block at player is that block and block below player is the one under it. North is -z, south is +z, east is +x and west is -x.",
        "Like Skript's block %direction% %location%, any location works in place of the player: block above target block, block 2 below {home} or block north of location(0, 64, 0). The direction is counted from the block that contains the location. A location in another dimension has no block: the result is none. For the block at a location itself, see Block At Location."
})
@Examples({
        "on key press of \"r\":",
        "\tif block below player is stone:",
        "\t\tsend \"standing on stone\"",
        "",
        "on key press of \"r\":",
        "\tsend \"2 blocks up: %block 2 above player%, north: %block north of player%\"",
        "",
        "on key press of \"r\":",
        "\tif block above target block is air:",
        "\t\tsend \"nothing on top of it\""
})
@Since({"1.0.0-alpha", "1.0.0-alpha.11"})
public final class ExprBlock implements Expression {
    private static final String DISTANCE = "[the] block [%-number% [(block|blocks)]] ";
    private static final String AROUND = "above:above|above:over|above:up|below:below|below:under|below:beneath"
            + "|below:down|north:north|south:south|east:east|west:west";
    private static final String PATTERN = DISTANCE + "(" + AROUND + "|at:at) [of] [the] [(player|me|myself)]";
    private static final String AT_LOCATION = DISTANCE + "(" + AROUND + ") [of] %location%";
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
    private final Expression location;

    private ExprBlock(Expression distance, int[] direction, Expression location) {
        this.distance = distance;
        this.direction = direction;
        this.location = location;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.BLOCK, Tier.COMBINED, ExprBlock::create, PATTERN, AT_LOCATION);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        String tag = DIRECTIONS.keySet().stream().filter(match::has).findFirst().orElse("at");
        Expression distance = match.slot(0);
        Expression location = match.slots().size() > 1 ? match.slot(1) : null;
        if (distance != null && distance.isList() || location != null && location.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprBlock(distance, DIRECTIONS.get(tag), location));
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
        if (location != null) {
            Location from = Locations.read(location, context);
            if (!Locations.isHere(from, context)) {
                return None.NONE;
            }
            Location target = from.blockCorner().offset(dx, dy, dz);
            return new BlockValue(world.blockAt(target.x(), target.y(), target.z()), target);
        }
        Location feet = new Location(world.playerX(), world.playerY(), world.playerZ(), world.dimension());
        return new BlockValue(world.blockIdAt(dx, dy, dz), feet.blockCorner().offset(dx, dy, dz));
    }
}
