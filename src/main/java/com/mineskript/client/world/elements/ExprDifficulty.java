package com.mineskript.client.world.elements;

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

@Name("Difficulty")
@Description("The world difficulty as lower case text: peaceful, easy, normal or hard. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on world join:",
        "\tsend \"difficulty: %difficulty%\""
})
@Since("1.0.0-alpha.2")
public final class ExprDifficulty extends GameValueExpression {
    private ExprDifficulty() {
        super(SkType.TEXT, GameBridge::difficulty);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprDifficulty()), "[the] difficulty");
    }
}
