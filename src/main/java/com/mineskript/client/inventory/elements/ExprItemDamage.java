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

@Name("Item Damage")
@Description("How much durability an item has lost, as a whole number: 0 on a brand new tool, rising as it wears down. Items without durability give 0. This is Skript's damage; the uses left are durability. If the value is not an item the line stops with a \"there is no item\" error.")
@Examples({
        "on key press of \"d\":",
        "	set {_left} to max damage of held item - damage of held item",
        "	send \"%{_left}% uses left\""
})
@Since("1.0.0-alpha.2")
public final class ExprItemDamage extends ItemPropertyExpression {
    private ExprItemDamage(Expression item) {
        super(SkType.NUMBER, value -> (double) value.damage(), item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemDamage(match.slot(0))),
                "[the] damage of %item%",
                "%item%'s damage");
    }
}
