package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Total Experience")
@Description({
        "The total experience points you have collected, as a whole number, as tracked by the game's total experience counter. Also written total experience, and like Skript experience of player or player's total experience. Needs a world: outside a world the line stops with a \"no world\" error.",
        "This is the running total, not the points needed for the next level. Use xp level and xp progress for the bar."
})
@Examples({
        "on xp change:",
        "\tsend \"total xp: %total xp%\"",
        "",
        "on key press of \"x\":",
        "\tsend \"you have %player's total experience% experience points\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprTotalExperience implements Expression {
    private ExprTotalExperience() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprTotalExperience()),
                "[the] (total xp|total experience) [of %players%]",
                "[the] experience of %players%",
                "%players%'s [total] experience",
                "%players%'s total xp");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().totalExperience();
    }
}
