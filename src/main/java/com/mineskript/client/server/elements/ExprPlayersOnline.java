package com.mineskript.client.server.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Players Online")
@Description({
        "The number of players in the world your client has loaded, yourself included, as a whole number. Needs a world: outside a world the line stops with a \"no world\" error.",
        "This counts players within render distance, not the whole server. For everyone on the server, loop online player names."
})
@Examples({
        "on key press of \"p\":",
        "\tsend \"%players online% players loaded nearby\""
})
@Since("1.0.0-alpha.2")
public final class ExprPlayersOnline extends GameValueExpression {
    private ExprPlayersOnline() {
        super(SkType.NUMBER, game -> (double) game.playersOnline());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprPlayersOnline()), "[the] players online");
    }
}
