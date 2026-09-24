package com.mineskript.client.world.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Sky Light")
@Description({
        "The sky light level at your feet, a whole number from 0 to 15, where 15 means open to the sky. It is the raw sky light value, so it does not drop at night or in rain. Also written sky light level, and like Skript sunlight level. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Sky light level of a location gives the sky light there instead, such as sky light level of target block. A location in another dimension gives none."
})
@Examples({
        "on key press of \"l\":",
        "	send \"block light %block light level%, sky light %sky light%\"",
        "",
        "on key press of \"l\":",
        "	if sunlight level of block above player is 15:",
        "		send \"open sky above\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprSkyLight implements Expression {
    private final Expression location;

    private ExprSkyLight(Expression location) {
        this.location = location;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> create(match.slot(0)),
                "[the] (sky light|sky light level|sky lightlevel|skylight level|sun light level|sunlight level) "
                        + "[of %location%]",
                "%location%'s (sky light|sky light level|sunlight level)");
    }

    private static Optional<Expression> create(Expression location) {
        if (location != null && location.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprSkyLight(location));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        if (location == null) {
            return (double) context.world().skyLight();
        }
        return LightAt.read(location, context, true);
    }
}
