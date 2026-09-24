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

@Name("Item ID")
@Description("The namespaced id of an item as text, such as minecraft:diamond_pickaxe. It ignores custom names, so it is the reliable way to tell items apart. An empty stack gives minecraft:air. If the value is not an item the line stops with a \"there is no item\" error.")
@Examples({
        "on key press of \"n\":",
        "	if id of held item is \"minecraft:diamond_pickaxe\":",
        "		send \"diamond pick in hand\""
})
@Since("1.0.0-alpha.2")
public final class ExprItemId extends ItemPropertyExpression {
    private ExprItemId(Expression item) {
        super(SkType.TEXT, ItemValue::id, item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemId(match.slot(0))),
                "[the] id of %item%",
                "%item%'s id");
    }
}
