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

@Name("XP Progress")
@Description("How far you are towards the next level, as a decimal from 0 (empty bar) to 1 (full bar). Also written experience progress, and like Skript level progress, level progress of player or player's level progress. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"x\":",
        "\tsend \"level %xp level%, %xp progress * 100% percent to the next\"",
        "",
        "every 1 second:",
        "\tif player's level progress is greater than 0.9:",
        "\t\tshow action bar \"almost the next level\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprXpProgress extends GameValueExpression {
    private ExprXpProgress() {
        super(SkType.NUMBER, GameBridge::xpProgress);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprXpProgress()),
                "[the] (xp progress|experience progress|level progress) [of %players%]",
                "%players%'s (xp progress|experience progress|level progress)");
    }
}
