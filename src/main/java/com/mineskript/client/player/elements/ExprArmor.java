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

@Name("Armor")
@Description("Your armor points as a whole number, the value the armor bar shows: each icon on the bar is 2 points and a full bar is 20. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"a\":",
        "	send \"armor points: %armor%\""
})
@Since("1.0.0-alpha.2")
public final class ExprArmor extends GameValueExpression {
    private ExprArmor() {
        super(SkType.NUMBER, game -> (double) game.armor());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprArmor()), "[the] armor");
    }
}
