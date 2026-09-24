package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
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

@Name("Relative Location")
@Description({"A location a number of blocks away from another one in a direction: above (also over), below (also under or beneath), north, south, east or west. Written like Skript, as the location 2 meters above the player or the location north of {home}, or shorter as 2 above the player or 3 blocks north of {home} wherever a location is expected.",
        "Without a number the distance is 1 block. The number is not rounded, so 0.5 above is half a block up. North is -z, south is +z, east is +x and west is -x. The new location is in the same dimension as the one it starts from."})
@Examples({"on key press of \"b\":",
        "	show a \"red\" beam at the location 3 blocks north of player",
        "",
        "on key press of \"h\":",
        "	spawn a hologram \"&6here\" at 2 above player",
        "",
        "on key press of \"k\":",
        "	send \"%location 1.5 meters below location(0, 64, 0)%\""})
@Since("1.0.0-alpha.10")
public final class ExprRelativeLocation implements Expression {
    private static final String DIRECTIONS = "(up:above|up:over|down:below|down:under|down:beneath"
            + "|north:north [of]|south:south [of]|east:east [of]|west:west [of])";
    private static final String SHORT_DIRECTIONS = "(up:above|up:over|down:below|down:under|down:beneath"
            + "|north:north of|south:south of|east:east of|west:west of)";
    private static final Map<String, double[]> STEPS = Map.of(
            "up", new double[] {0, 1, 0},
            "down", new double[] {0, -1, 0},
            "north", new double[] {0, 0, -1},
            "south", new double[] {0, 0, 1},
            "east", new double[] {1, 0, 0},
            "west", new double[] {-1, 0, 0});

    private final Expression distance;
    private final double[] step;
    private final Expression origin;

    private ExprRelativeLocation(Expression distance, double[] step, Expression origin) {
        this.distance = distance;
        this.step = step;
        this.origin = origin;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.LOCATION, Tier.COMBINED, ExprRelativeLocation::create,
                "[the] (location|position) [%-number% [(block|blocks|meter|meters)]] " + DIRECTIONS + " %location%",
                "%number% [(block|blocks|meter|meters)] " + SHORT_DIRECTIONS + " %location%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        Expression distance = match.slot(0);
        Expression origin = match.slot(1);
        if (distance != null && distance.isList() || origin.isList()) {
            return Optional.empty();
        }
        String direction = STEPS.keySet().stream().filter(match::has).findFirst().orElseThrow();
        return Optional.of(new ExprRelativeLocation(distance, STEPS.get(direction), origin));
    }

    @Override
    public SkType type() {
        return SkType.LOCATION;
    }

    @Override
    public Object evaluate(Context context) {
        if (!(origin.evaluate(context) instanceof Location start)) {
            return None.NONE;
        }
        double blocks = distance == null ? 1 : (Double) distance.evaluate(context);
        return start.offset(step[0] * blocks, step[1] * blocks, step[2] * blocks);
    }
}
