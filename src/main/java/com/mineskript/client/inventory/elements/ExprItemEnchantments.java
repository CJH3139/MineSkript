package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Enchantment;
import com.mineskript.lang.ast.EnchantmentType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Name("Item Enchantments")
@Description({"The enchantments of an item as a list of enchantment types, like Skript: each one the enchantment and its level, printed as sharpness 5 or fire aspect 2, sorted by id. Enchanted books give the enchantments stored in them. Enchantments from other mods or data packs keep their namespace, such as mymod:frost 1. An item without enchantments gives an empty list.",
        "Compare an entry with an enchantment type written out, as in loop-value is sharpness 5, or with text such as \"sharpness 5\". Use level of sharpness of an item to get one level as a number, and is enchanted with to check for one. Items read back from saved variables have no enchantments. If the value is not an item the line stops with a \"there is no item\" error."})
@Examples({"on key press of \"e\":",
        "	send \"enchantments: %enchantments of held item%\"",
        "",
        "on key press of \"e\":",
        "	loop enchantments of held item:",
        "		send \"has %loop-value%\""})
@Since({"1.0.0-alpha.9", "1.0.0-alpha.11"})
public final class ExprItemEnchantments extends ItemPropertyExpression {
    private ExprItemEnchantments(Expression item) {
        super(SkType.ENCHANTMENTTYPE, ExprItemEnchantments::describe, item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ENCHANTMENTTYPE, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemEnchantments(match.slot(0))),
                "[the] enchantments of %item%",
                "%item%'s enchantments");
    }

    private static Object describe(ItemValue item) {
        List<EnchantmentType> types = new ArrayList<>();
        for (Map.Entry<String, Integer> enchantment : item.details().enchantments().entrySet()) {
            types.add(new EnchantmentType(new Enchantment(enchantment.getKey()), enchantment.getValue()));
        }
        return List.copyOf(types);
    }

    @Override
    public boolean isList() {
        return true;
    }
}
