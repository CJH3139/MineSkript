package com.mineskript.client.entity.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Entity Distance")
@Description("How far an entity is from you in blocks, as a decimal number, measured between your position and the entity's position. The distance is captured when the entity value is read and is not updated later. If the value is not an entity the line stops with a \"there is no entity\" error.")
@Examples({
        "every 1 second:",
        "\tif nearest player is set:",
        "\t\tshow action bar \"%name of nearest player%: %distance of nearest player% blocks\""
})
@Since("1.0.0-alpha.2")
public final class ExprEntityDistance extends EntityPropertyExpression {
    private ExprEntityDistance(Expression entity) {
        super(SkType.NUMBER, EntityValue::distance, entity);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprEntityDistance(match.slot(0))),
                "[the] distance of %entity%",
                "%entity%'s distance");
    }
}
