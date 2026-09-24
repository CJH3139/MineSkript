package com.mineskript.client.player.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.common.elements.expressions.ExprEventValue;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Fall Distance")
@Description({
        "How many blocks you have fallen since last touching the ground, as a decimal number. Also written fallen distance, fall height, fall distance of player or player's fall distance, like Skript. Needs a world: outside a world the line stops with a \"no world\" error.",
        "The game resets it to 0 when you land, so inside on land it gives how far you fell before landing instead, the same number as event-fall distance."
})
@Examples({
        "every 5 ticks:",
        "\tif fall distance is greater than 10:",
        "\t\tshow title \"falling!\"",
        "",
        "on land:",
        "\tif fall distance is greater than 3:",
        "\t\tsend \"fell %fall distance% blocks\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprFallDistance extends GameValueExpression {
    private static final String NAMES = "(fall distance|fallen distance|fall height|fallen height)";

    private ExprFallDistance() {
        super(SkType.NUMBER, GameBridge::fallDistance);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, ExprFallDistance::create,
                "[the] " + NAMES + " [of %player%]",
                "%player%'s " + NAMES);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        Event event = scope.event();
        if (event != null && event.context().provides("fall distance")) {
            return Optional.of(ExprEventValue.reading("fall distance", SkType.NUMBER));
        }
        return Optional.of(new ExprFallDistance());
    }
}
