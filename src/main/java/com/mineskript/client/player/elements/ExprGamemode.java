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

@Name("Gamemode")
@Description("Your current game mode as lower case text: survival, creative, adventure or spectator. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on gamemode change:",
        "\tsend \"now in %gamemode%\"",
        "",
        "on key press of \"g\":",
        "\tif gamemode is \"creative\":",
        "\t\tsend \"creative mode\""
})
@Since("1.0.0-alpha.2")
public final class ExprGamemode extends GameValueExpression {
    private ExprGamemode() {
        super(SkType.TEXT, GameBridge::gamemode);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprGamemode()), "[the] gamemode");
    }
}
