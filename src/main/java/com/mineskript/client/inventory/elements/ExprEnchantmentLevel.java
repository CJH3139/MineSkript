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
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;

@Name("Enchantment Level")
@Description({"The level of one enchantment on an item, as a number, or 0 when the item does not have it. The enchantment is written as text, such as \"sharpness\", \"fire aspect\" or \"minecraft:fire_aspect\"; enchantments from other mods need their namespace, such as \"mymod:frost\". Enchanted books count the enchantments stored in them.",
        "Items read back from saved variables have no enchantments. If the value is not an item the line stops with a \"there is no item\" error."})
@Examples({"on key press of \"e\":",
        "\tif level of enchantment \"efficiency\" on held item is less than 5:",
        "\t\tsend \"not a max efficiency tool\"",
        "",
        "on item tooltip:",
        "\tset {_mending} to enchantment level of \"mending\" on event-item",
        "\tif {_mending} is 1:",
        "\t\tadd \"&amending\" to the tooltip"})
@Since("1.0.0-alpha.9")
public final class ExprEnchantmentLevel implements Expression {
    private final Expression enchantment;
    private final Expression item;

    private ExprEnchantmentLevel(Expression enchantment, Expression item) {
        this.enchantment = enchantment;
        this.item = item;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY,
                (match, scope) -> match.slot(0).isList() ? Optional.empty()
                        : Optional.of(new ExprEnchantmentLevel(match.slot(0), match.slot(1))),
                "[the] level of enchantment %string% on %item%",
                "[the] enchantment level of %string% on %item%");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        String name = (String) enchantment.evaluate(context);
        if (!(item.evaluate(context) instanceof ItemValue found)) {
            throw new ScriptError("there is no item");
        }
        return (double) found.details().enchantments().getOrDefault(Enchantments.id(name), 0);
    }
}
