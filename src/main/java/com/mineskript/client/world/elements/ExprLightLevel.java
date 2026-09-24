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
        "The light level at your feet, a whole number from 0 to 15. Like Skript, a plain light level is the brightest of the block light and the sky light, with the sky light dimmed at night and in rain, which is the number mob spawning goes by. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Block light level is only the light from blocks such as torches and lava, and sky light level (or sunlight level) is only the light from the sky; see Sky Light.",
        "Light level of a location, or block light level of a location, gives the light there instead, such as light level of target block. A location in another dimension gives none."
})
@Examples({
        "every 2 seconds:",
        "	if light level is 0:",
        "		send action bar \"dark here, mobs can spawn\"",
        "",
        "on key press of \"l\":",
        "	send \"light %light level of target block%, from blocks %block light level of target block%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11", "1.0.0-alpha.12"})
public final class ExprLightLevel implements Expression {
    private final Expression location;
    private final boolean blockOnly;

    private ExprLightLevel(Expression location, boolean blockOnly) {
        this.location = location;
        this.blockOnly = blockOnly;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> create(match.slot(0), match.has("block")),
                "[the] [block:block] (light level|lightlevel) [of %location%]",
                "[the] block:blocklight level [of %location%]",
                "%location%'s [block:block] light level");
    }

    private static Optional<Expression> create(Expression location, boolean blockOnly) {
        if (location != null && location.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprLightLevel(location, blockOnly));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        if (location == null) {
            return (double) (blockOnly ? context.world().lightLevel() : context.world().combinedLight());
        }
        return blockOnly ? LightAt.read(location, context, false) : LightAt.readCombined(location, context);
    }
}
