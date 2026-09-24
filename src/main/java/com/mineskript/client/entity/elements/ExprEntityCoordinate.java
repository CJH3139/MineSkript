package com.mineskript.client.entity.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;
import java.util.function.Function;

@Name("Entity Coordinate")
@Description("The exact x, y or z position of an entity, as a decimal number (y is the bottom of the entity). Accepts x-coordinate, x-coord, x coordinate or x coord, and the same for y and z. The position is the one captured when the entity value was read, so a copy stored in a variable does not follow the entity. If the value is not an entity the line stops with a \"there is no entity\" error. A variable holding an entity also works: its coordinates are read through its location (see Location Coordinate).")
@Examples({
        "on key press of \"e\":",
        "\tif target entity is set:",
        "\t\tsend \"at %x-coordinate of target entity%, %y-coordinate of target entity%, %z-coordinate of target entity%\""
})
@Since("1.0.0-alpha.2")
public final class ExprEntityCoordinate extends EntityPropertyExpression {
    private static final String AXES = "(x:x-coordinate|x:x-coord|x:x coordinate|x:x coord|y:y-coordinate|y:y-coord|y:y coordinate|y:y coord|z:z-coordinate|z:z-coord|z:z coordinate|z:z coord)";

    private ExprEntityCoordinate(Function<EntityValue, Object> reader, Expression entity) {
        super(SkType.NUMBER, reader, entity);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, Priority.after(Priority.SIMPLE),
                ExprEntityCoordinate::create,
                "[the] " + AXES + " of %entity%",
                "%entity%'s " + AXES);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0) instanceof ConvertedExpression converted && converted.untyped()) {
            return Optional.empty();
        }
        Function<EntityValue, Object> reader;
        if (match.has("x")) {
            reader = EntityValue::x;
        } else if (match.has("y")) {
            reader = EntityValue::y;
        } else {
            reader = EntityValue::z;
        }
        return Optional.of(new ExprEntityCoordinate(reader, match.slot(0)));
    }
}
