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

@Name("Speed")
@Description("Your current horizontal speed in blocks per tick, as a decimal number. Vertical movement (falling or jumping) is not included. Multiply by 20 for blocks per second. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"s\":",
        "\tsend \"%speed * 20% blocks per second\""
})
@Since("1.0.0-alpha.2")
public final class ExprSpeed extends GameValueExpression {
    private ExprSpeed() {
        super(SkType.NUMBER, GameBridge::speed);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprSpeed()), "[the] speed");
    }
}
