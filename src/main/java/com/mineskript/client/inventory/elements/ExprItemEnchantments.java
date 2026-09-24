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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Name("Item Enchantments")
@Description({"The enchantments of an item as a list of text, each one the enchantment and its level, such as sharpness 5 or fire aspect 2, sorted by id. Enchanted books give the enchantments stored in them. Enchantments from other mods or data packs keep their namespace, such as mymod:frost 1. An item without enchantments gives an empty list.",
        "Use level of enchantment to get one level as a number. Items read back from saved variables have no enchantments. If the value is not an item the line stops with a \"there is no item\" error."})
@Examples({"on key press of \"e\":",
        "\tsend \"enchantments: %enchantments of held item%\""})
@Since("1.0.0-alpha.9")
public final class ExprItemEnchantments extends ItemPropertyExpression {
    private ExprItemEnchantments(Expression item) {
        super(SkType.TEXT, ExprItemEnchantments::describe, item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemEnchantments(match.slot(0))),
                "[the] enchantments of %item%",
                "%item%'s enchantments");
    }

    private static Object describe(ItemValue item) {
        List<String> texts = new ArrayList<>();
        for (Map.Entry<String, Integer> enchantment : item.details().enchantments().entrySet()) {
            texts.add(Enchantments.displayName(enchantment.getKey()) + " " + enchantment.getValue());
        }
        return List.copyOf(texts);
    }

    @Override
    public boolean isList() {
        return true;
    }
}
