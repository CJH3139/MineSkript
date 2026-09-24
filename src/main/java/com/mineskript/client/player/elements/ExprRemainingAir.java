package com.mineskript.client.player.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Remaining Air")
@Description({
        "How long you can stay underwater before you start to drown, as a time span, like Skript's remaining air: 15 seconds when you are not underwater, counting down while you are submerged. Written remaining air, remaining air of player or player's remaining air. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Compare it with a time, as in remaining air is less than 3 seconds. Air gives the same amount as a number of ticks."
})
@Examples({
        "every 1 second:",
        "\tif the player's remaining air is less than 3 seconds:",
        "\t\tshow title \"get to the surface!\""
})
@Since("1.0.0-alpha.11")
public final class ExprRemainingAir extends GameValueExpression {
    private ExprRemainingAir() {
        super(SkType.TIMESPAN, game -> new Timespan(Math.max(0, game.air())));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TIMESPAN, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprRemainingAir()),
                "[the] remaining air [of %players%]",
                "%players%'s remaining air");
    }
}
