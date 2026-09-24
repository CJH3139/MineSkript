package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Player Coordinate")
@Description({
        "Your exact x, y or z position as a decimal number (y is the height of your feet). Accepts x-coordinate, x-coord, x coordinate or x coord, and the same for y and z, written either as x-coordinate of player or player's x-coordinate. Printed in text it is rounded to at most 2 decimal places. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Only your own player exists in MineSkript, so the player part always means you. Use round or floor for block coordinates."
})
@Examples({
        "on key press of \"c\":",
        "	send \"at %player's x-coordinate%, %player's y-coordinate%, %player's z-coordinate%\"",
        "",
        "every 1 second:",
        "	if y-coordinate of player is less than 0:",
        "		show action bar \"below sea of deepslate\""
})
@Since("1.0.0-alpha")
public final class ExprCoordinate implements Expression {
    private static final String AXES = "(x:x-coordinate|x:x-coord|x:x coordinate|x:x coord|y:y-coordinate|y:y-coord|y:y coordinate|y:y coord|z:z-coordinate|z:z-coord|z:z coordinate|z:z coord)";

    private final char axis;

    private ExprCoordinate(char axis) {
        this.axis = axis;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, Priority.before(Priority.COMBINED), ExprCoordinate::create,
                "[the] " + AXES + " of %player%",
                "%player%'s " + AXES);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0) instanceof ConvertedExpression converted && converted.untyped()) {
            return Optional.empty();
        }
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
