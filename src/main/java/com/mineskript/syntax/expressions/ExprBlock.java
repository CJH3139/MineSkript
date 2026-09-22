package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Map;
import java.util.Optional;

public final class ExprBlock implements Expression {
    private static final String PATTERN = "[the] block [%-number% [(block|blocks)]] "
            + "(above:above|above:over|above:up|below:below|below:under|below:beneath|below:down|north:north|south:south|east:east|west:west|at:at) [of] [the] [player]";
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
        String id = context.world().blockIdAt(direction[0] * steps, direction[1] * steps, direction[2] * steps);
        return new BlockValue(id);
    }
}
