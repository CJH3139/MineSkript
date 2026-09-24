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

@Name("Durability")
@Description({
        "How many uses an item has left, as a whole number, like Skript's durability: its max durability minus its damage, so a new diamond pickaxe has 1561 and a worn one less. Items without durability give 0. Written durability of held item or held item's durability. If the value is not an item the line stops with a \"there is no item\" error.",
        "Damage gives the opposite, the durability lost so far."
})
@Examples({
        "every 5 seconds:",
        "	if durability of held item is less than 20:",
        "		if max durability of held item is greater than 0:",
        "			show title \"tool almost broken\"",
        "",
        "on key press of \"d\":",
        "	send \"%held item's durability% uses left\""
})
@Since("1.0.0-alpha.11")
public final class ExprDurability extends ItemPropertyExpression {
    private ExprDurability(Expression item) {
        super(SkType.NUMBER, value -> (double) Math.max(0, value.maxDamage() - value.damage()), item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprDurability(match.slot(0))),
                "[the] (durability|durabilities) of %item%",
                "%item%'s (durability|durabilities)");
    }
}
