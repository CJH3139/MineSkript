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

@Name("XP Level")
@Description("Your experience level as a whole number, the green number above the hotbar. Also written experience level. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on level up:",
        "\tsend \"reached level %xp level%\""
})
@Since("1.0.0-alpha.2")
public final class ExprXpLevel extends GameValueExpression {
    private ExprXpLevel() {
        super(SkType.NUMBER, game -> (double) game.xpLevel());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprXpLevel()), "[the] (xp level|experience level)");
    }
}
