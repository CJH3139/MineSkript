package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;

public final class ExprTargetCoordinate implements Expression {
    private static final String AXES = "(x:x-coordinate|x:x-coord|x:x coordinate|x:x coord|y:y-coordinate|y:y-coord|y:y coordinate|y:y coord|z:z-coordinate|z:z-coord|z:z coordinate|z:z coord)";

    private final int axis;

    private ExprTargetCoordinate(int axis) {
        this.axis = axis;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, ExprTargetCoordinate::create,
                "[the] " + AXES + " of [the] target block");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        int axis = match.has("x") ? 0 : match.has("y") ? 1 : 2;
        return Optional.of(new ExprTargetCoordinate(axis));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        int[] position = context.world().targetBlockPosition();
        if (position == null) {
            throw new ScriptError("there is no target block");
        }
        return (double) position[axis];
    }
}
