package com.mineskript.client.entity.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Nearest Player")
@Description("The closest other player whose entity your client has loaded (players within render distance, not the whole tab list). Returns none when nobody else is nearby. The value is an entity snapshot, so use entity properties like name of and distance of. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "every 2 seconds:",
        "	if nearest player is set:",
        "		if nearest player is within 10 blocks:",
        "			show action bar \"%name of nearest player% is close\""
})
@Since("1.0.0-alpha.2")
public final class ExprNearestPlayer extends GameValueExpression {
    private ExprNearestPlayer() {
        super(SkType.ENTITY, game -> nullToNone(game.nearestPlayer()));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ENTITY, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprNearestPlayer()), "[the] nearest player");
    }
}
