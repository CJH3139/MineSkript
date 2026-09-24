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
@Description("The block light level at your feet, a whole number from 0 to 15. This is only light from blocks such as torches and lava; sunlight is not included (see sky light). Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "every 2 seconds:",
        "\tif light level is less than 8:",
        "\t\tshow action bar \"dark here, mobs can spawn\""
})
@Since("1.0.0-alpha.2")
public final class ExprLightLevel implements Expression {
    private ExprLightLevel() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprLightLevel()), "[the] light level");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().lightLevel();
    }
}
