package com.mineskript.client.player.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.GameMode;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Gamemode")
@Description({
        "Your current game mode: survival, creative, adventure or spectator. Written gamemode, game mode, gamemode of player or player's gamemode, like Skript. Needs a world: outside a world the line stops with a \"no world\" error.",
        "It is a game mode value, so compare it with a game mode written out, as in player's gamemode is creative. Text still works too: gamemode is \"creative\" is true in creative mode, and printed in text it shows the lower case name."
})
@Examples({
        "on gamemode change:",
        "	send \"now in %gamemode%\"",
        "",
        "on key press of \"g\":",
        "	if player's gamemode is creative:",
        "		send \"creative mode\"",
        "",
        "on key press of \"g\":",
        "	if game mode of player is survival or adventure:",
        "		send \"you can be hurt\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprGamemode extends GameValueExpression {
    private static final String NAMES = "(gamemode|game mode)";

    private ExprGamemode() {
        super(SkType.GAMEMODE, ExprGamemode::read);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.GAMEMODE, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprGamemode()),
                "[the] " + NAMES + " [of %players%]",
                "%players%'s " + NAMES);
    }

    private static Object read(GameBridge game) {
        String name = game.gamemode();
        return GameMode.parse(name).<Object>map(mode -> mode).orElse(name);
    }
}
