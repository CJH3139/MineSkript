package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Item Count")
@Description("How many items are in a stack, as a whole number. Also written amount of. An empty stack gives 0. If the value is not an item the line stops with a \"there is no item\" error.")
@Examples({
        "on key press of \"c\":",
        "\tsend \"%count of held item% in hand, %amount of offhand item% in offhand\""
})
@Since("1.0.0-alpha.2")
public final class ExprItemCount extends ItemPropertyExpression {
    private ExprItemCount(Expression item) {
        super(SkType.NUMBER, value -> (double) value.count(), item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemCount(match.slot(0))),
                "[the] (count|amount) of %item%",
                "%item%'s (count|amount)");
    }
}
