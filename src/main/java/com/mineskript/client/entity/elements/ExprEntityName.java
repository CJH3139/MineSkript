package com.mineskript.client.entity.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Entity Name")
@Description("The display name of an entity as text, such as Zombie, a player's username, or a custom name tag. If the value is not an entity (for example target entity when looking at nothing) the line stops with a \"there is no entity\" error. Written name of X or X's name.")
@Examples({
        "on key press of \"e\":",
        "\tif target entity is set:",
        "\t\tsend \"that is %name of target entity%\""
})
@Since("1.0.0-alpha.2")
public final class ExprEntityName extends EntityPropertyExpression {
    private ExprEntityName(Expression entity) {
        super(SkType.TEXT, EntityValue::name, entity);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY, Priority.after(Priority.SIMPLE),
                (match, scope) -> Optional.of(new ExprEntityName(match.slot(0))),
                "[the] name of %entity%",
                "%entity%'s name");
    }
}
