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

@Name("Target Entity")
@Description("The entity your crosshair is on, such as a mob, player, item frame or boat. Returns none (prints as <none>) when you are not looking at an entity within reach. The value is a snapshot of that moment. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"e\":",
        "\tif target entity is set:",
        "\t\tsend \"looking at %name of target entity% %distance of target entity% blocks away\"",
        "\telse:",
        "\t\tsend \"nothing targeted\""
})
@Since("1.0.0-alpha.2")
public final class ExprTargetEntity extends GameValueExpression {
    private ExprTargetEntity() {
        super(SkType.ENTITY, game -> nullToNone(game.targetEntity()));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ENTITY, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprTargetEntity()), "[the] target entity");
    }
}
