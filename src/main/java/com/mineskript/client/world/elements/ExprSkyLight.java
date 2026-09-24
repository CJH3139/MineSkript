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
@Description("The sky light level at your feet, a whole number from 0 to 15, where 15 means open to the sky. It is the raw sky light value, so it does not drop at night or in rain. Also written sky light level. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"l\":",
        "\tsend \"block light %light level%, sky light %sky light%\""
})
@Since("1.0.0-alpha.2")
public final class ExprSkyLight implements Expression {
    private ExprSkyLight() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprSkyLight()), "[the] (sky light|sky light level)");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().skyLight();
    }
}
