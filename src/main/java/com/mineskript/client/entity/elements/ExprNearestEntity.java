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

@Name("Nearest Entity")
@Description("The closest entity to you, excluding yourself, out of every entity your client has loaded. That includes mobs, other players, dropped items, arrows and experience orbs, with no distance limit. Returns none when there are no other entities. The value is a snapshot taken when read. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"e\":",
        "\tif nearest entity is set:",
        "\t\tsend \"closest: %id of nearest entity% at %distance of nearest entity% blocks\""
})
@Since("1.0.0-alpha.2")
public final class ExprNearestEntity extends GameValueExpression {
    private ExprNearestEntity() {
        super(SkType.ENTITY, game -> nullToNone(game.nearestEntity()));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ENTITY, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprNearestEntity()), "[the] nearest entity");
    }
}
