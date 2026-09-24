package com.mineskript.client.player.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Max Air")
@Description("Your maximum air supply in ticks as a whole number, normally 300 (15 seconds). Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"a\":",
        "\tsend \"air %air% of %max air%\""
})
@Since("1.0.0-alpha.2")
public final class ExprMaxAir extends GameValueExpression {
    private ExprMaxAir() {
        super(SkType.NUMBER, game -> (double) game.maxAir());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprMaxAir()), "[the] max air");
    }
}
