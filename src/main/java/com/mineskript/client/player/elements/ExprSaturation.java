package com.mineskript.client.player.elements;

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

@Name("Saturation")
@Description("Your food saturation level, a decimal number from 0 up to your hunger level (at most 20). Hunger only starts dropping once saturation reaches 0. Written saturation, saturation of player or player's saturation, like Skript. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"h\":",
        "	send \"hunger %hunger of player%, saturation %saturation%\"",
        "",
        "every 5 seconds:",
        "	if player's saturation is 0:",
        "		show action bar \"hunger will start to drop\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprSaturation implements Expression {
    private ExprSaturation() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprSaturation()),
                "[the] saturation [of %players%]",
                "%players%'s saturation");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().saturation();
    }
}
