package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Gamemode Is")
@Description({
        "Checks your current game mode against a text value, ignoring case. The game reports survival, creative, adventure or spectator.",
        "A value that is not text, or a name that is not a game mode, is simply never equal. The gamemode is also a game mode value, so gamemode is creative and player's gamemode is not survival work without quotes, like in Skript (see Gamemode)."
})
@Examples({
        "on key press of \"g\":",
        "	if gamemode is \"creative\":",
        "		send \"creative mode, flying is fine\"",
        "",
        "on world join:",
        "	if gamemode is not \"survival\":",
        "		send \"not in survival\""
})
@Since("1.0.0-alpha.2")
public final class CondGamemode implements Condition {
    private final Expression mode;
    private final boolean negate;

    private CondGamemode(Expression mode, boolean negate) {
        this.mode = mode;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "[the] gamemode (is|are) %string%",
                "[the] gamemode (isn't|is not|aren't|are not) %string%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression mode = match.slot(0);
        if (mode.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondGamemode(mode, negate));
    }

    @Override
    public boolean test(Context context) {
        Object value = mode.evaluate(context);
        boolean result = value instanceof String text && text.equalsIgnoreCase(context.world().gamemode());
        return negate != result;
    }
}
