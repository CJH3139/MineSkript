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
@Description("How far you are towards the next level, as a decimal from 0 (empty bar) to 1 (full bar). Also written experience progress. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"x\":",
        "\tsend \"level %xp level%, %xp progress * 100% percent to the next\""
})
@Since("1.0.0-alpha.2")
public final class ExprXpProgress extends GameValueExpression {
    private ExprXpProgress() {
        super(SkType.NUMBER, GameBridge::xpProgress);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprXpProgress()), "[the] (xp progress|experience progress)");
    }
}
