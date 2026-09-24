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

@Name("Air")
@Description("Your remaining air supply in ticks as a whole number. It is 300 (15 seconds) when you are not underwater and counts down while you are submerged. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "every 1 second:",
        "\tif air is less than 100:",
        "\t\tshow title \"surface now\""
})
@Since("1.0.0-alpha.2")
public final class ExprAir extends GameValueExpression {
    private ExprAir() {
        super(SkType.NUMBER, game -> (double) game.air());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprAir()), "[the] air");
    }
}
