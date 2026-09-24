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

@Name("Item Max Damage")
@Description("The total durability of an item, as a whole number (1561 for a diamond pickaxe). Items without durability give 0. Remaining durability is max damage minus damage, which durability gives directly. Also written max durability of or maximum durability of, like Skript. If the value is not an item the line stops with a \"there is no item\" error.")
@Examples({
        "every 5 seconds:",
        "	if max damage of held item is greater than 0:",
        "		if damage of held item is greater than max damage of held item - 20:",
        "			show title \"tool almost broken\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprItemMaxDamage extends ItemPropertyExpression {
    private ExprItemMaxDamage(Expression item) {
        super(SkType.NUMBER, value -> (double) value.maxDamage(), item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemMaxDamage(match.slot(0))),
                "[the] (max|maximum) (damage|durability) of %item%",
                "%item%'s (max|maximum) (damage|durability)");
    }
}
