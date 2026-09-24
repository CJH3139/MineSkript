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
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Location Coordinate")
@Description({"The x, y or z coordinate of a location, as a decimal number. Accepts x-coordinate, x-coord, x coordinate or x coord, and the same for y and z, written either as x-coordinate of {home} or {home}'s x-coordinate.",
        "It also works on anything that has a location, such as a variable holding an entity. The coordinates of the player, an entity or the target block written directly (player's x-coordinate, x-coordinate of target entity, x-coordinate of target block) are read by their own expressions and give the same numbers."})
@Examples({"on key press of \"h\":",
        "	set {home} to location of player",
        "",
        "on key press of \"j\":",
        "	if {home} is set:",
        "		send \"home is at height %{home}'s y-coordinate%\"",
        "",
        "on key press of \"k\":",
        "	send \"%x-coordinate of location(10, 64, -3)%\""})
@Since("1.0.0-alpha.10")
public final class ExprLocationCoordinate implements Expression {
    private static final String AXES = "(x:x-coordinate|x:x-coord|x:x coordinate|x:x coord"
            + "|y:y-coordinate|y:y-coord|y:y coordinate|y:y coord"
            + "|z:z-coordinate|z:z-coord|z:z coordinate|z:z coord)";

    private final Expression location;
    private final char axis;

    private ExprLocationCoordinate(Expression location, char axis) {
        this.location = location;
        this.axis = axis;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, Priority.COMBINED, ExprLocationCoordinate::create,
                "[the] " + AXES + " of %location%",
                "%location%'s " + AXES);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        char axis = match.has("x") ? 'x' : match.has("y") ? 'y' : 'z';
        return Optional.of(new ExprLocationCoordinate(match.slot(0), axis));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        if (!(location.evaluate(context) instanceof Location found)) {
            return None.NONE;
        }
        return switch (axis) {
            case 'x' -> found.x();
            case 'y' -> found.y();
            default -> found.z();
        };
    }
}
