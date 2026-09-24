package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Item Name")
@Description("The display name of an item as text, the same name its tooltip shows (so a renamed item gives its custom name). An empty stack gives air. If the value is not an item (for example an unset variable) the line stops with a \"there is no item\" error. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"n\":",
        "	send \"holding %name of held item%\""
})
@Since("1.0.0-alpha.2")
public final class ExprItemName extends ItemPropertyExpression {
    private ExprItemName(Expression item) {
        super(SkType.TEXT, ItemValue::name, item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemName(match.slot(0))),
                "[the] name of %item%",
                "%item%'s name");
    }
}
