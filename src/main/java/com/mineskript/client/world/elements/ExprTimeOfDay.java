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

@Name("Time Of Day")
@Description("The time of day in ticks, a whole number from 0 to 23999: 0 is sunrise, 6000 noon, 12000 sunset and 18000 midnight. Also written day time. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "every 10 seconds:",
        "\tif time of day is between 13000 and 23000:",
        "\t\tshow action bar \"night, mobs are out\""
})
@Since("1.0.0-alpha.6")
public final class ExprTimeOfDay extends GameValueExpression {
    private ExprTimeOfDay() {
        super(SkType.NUMBER, game -> (double) Math.floorMod(game.dayTime(), 24000L));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprTimeOfDay()), "[the] (time of day|day time)");
    }
}
