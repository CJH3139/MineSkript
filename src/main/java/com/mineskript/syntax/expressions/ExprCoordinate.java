package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprCoordinate implements Expression {
    private static final String AXES = "(x:x-coordinate|x:x-coord|x:x coordinate|x:x coord|y:y-coordinate|y:y-coord|y:y coordinate|y:y coord|z:z-coordinate|z:z-coord|z:z coordinate|z:z coord)";

    private final char axis;

    private ExprCoordinate(char axis) {
        this.axis = axis;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, ExprCoordinate::create,
                "[the] " + AXES + " of %player%",
                "%player%'s " + AXES);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        char axis = match.has("x") ? 'x' : match.has("y") ? 'y' : 'z';
        return Optional.of(new ExprCoordinate(axis));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return switch (axis) {
            case 'x' -> context.world().playerX();
            case 'y' -> context.world().playerY();
            default -> context.world().playerZ();
        };
    }
}
