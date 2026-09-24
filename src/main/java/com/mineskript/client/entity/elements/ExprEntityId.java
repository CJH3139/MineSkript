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

@Name("Entity ID")
@Description("The namespaced entity type id as text, such as minecraft:zombie, minecraft:player or minecraft:item. Unlike the name it ignores name tags, so use it to check what kind of entity something is. If the value is not an entity the line stops with a \"there is no entity\" error.")
@Examples({
        "every 1 second:",
        "\tif nearest entity is set:",
        "\t\tif id of nearest entity is \"minecraft:creeper\":",
        "\t\t\tshow title \"creeper nearby\""
})
@Since("1.0.0-alpha.2")
public final class ExprEntityId extends EntityPropertyExpression {
    private ExprEntityId(Expression entity) {
        super(SkType.TEXT, EntityValue::id, entity);
    }

    public static void register(SyntaxRegistry registry) {
        // After the item property of the same name, so the name, id or x-coordinate of a variable whose type is not
        // known is read as an item's first, then an entity's, then yours.
        registry.addExpression(SkType.TEXT, Tier.PROPERTY, Priority.after(Priority.SIMPLE),
                (match, scope) -> Optional.of(new ExprEntityId(match.slot(0))),
                "[the] id of %entity%",
                "%entity%'s id");
    }
}
