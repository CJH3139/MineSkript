package com.mineskript.client.world.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Game Time")
@Description("The world's total age in ticks as a whole number (20 ticks per second). It keeps counting up and is not affected by sleeping or time commands. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"t\":",
        "	send \"world age: %game time / 24000% days\""
})
@Since("1.0.0-alpha.2")
public final class ExprGameTime extends GameValueExpression {
    private ExprGameTime() {
        super(SkType.NUMBER, game -> (double) game.gameTime());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprGameTime()), "[the] game time");
    }
}
