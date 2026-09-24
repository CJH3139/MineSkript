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

@Name("Light Level")
@Description({
        "The block light level at your feet, a whole number from 0 to 15. This is only light from blocks such as torches and lava; sunlight is not included (see sky light). Needs a world: outside a world the line stops with a \"no world\" error.",
        "Like Skript, light level of a location, or block light level of a location, gives the light there instead, such as light level of target block. A location in another dimension gives none. Unlike Skript, where a plain light level mixes in sunlight, MineSkript's light level is always the block light, the same as block light level."
})
@Examples({
        "every 2 seconds:",
        "\tif light level is less than 8:",
        "\t\tshow action bar \"dark here, mobs can spawn\"",
        "",
        "on key press of \"l\":",
        "\tsend \"block light at the target: %block light level of target block%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprLightLevel implements Expression {
    private final Expression location;

    private ExprLightLevel(Expression location) {
        this.location = location;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> create(match.slot(0)),
                "[the] [block] (light level|lightlevel) [of %location%]",
                "[the] blocklight level [of %location%]",
                "%location%'s [block] light level");
    }

    private static Optional<Expression> create(Expression location) {
        if (location != null && location.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprLightLevel(location));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        if (location == null) {
            return (double) context.world().lightLevel();
        }
        return LightAt.read(location, context, false);
    }
}
