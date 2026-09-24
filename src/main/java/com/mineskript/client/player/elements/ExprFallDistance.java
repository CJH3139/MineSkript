package com.mineskript.client.player.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Fall Distance")
@Description("How many blocks you have fallen since last touching the ground, as a decimal number. It resets to 0 when you land. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "every 5 ticks:",
        "\tif fall distance is greater than 10:",
        "\t\tshow title \"falling!\""
})
@Since("1.0.0-alpha.2")
public final class ExprFallDistance extends GameValueExpression {
    private ExprFallDistance() {
        super(SkType.NUMBER, GameBridge::fallDistance);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprFallDistance()), "[the] fall distance");
    }
}
