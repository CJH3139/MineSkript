package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Enchantment;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;

@Name("Enchantment Level")
@Description({"The level of one enchantment on an item, as a number, or 0 when the item does not have it. Like Skript's enchantment level it is written level of sharpness of held item, held item's sharpness level or sharpness level of held item, with the enchantment written out: sharpness, fire aspect, luck of the sea, or an id such as minecraft:fire_aspect; enchantments from other mods need their namespace, such as mymod:frost. Enchanted books count the enchantments stored in them.",
        "The older form takes the enchantment as text: level of enchantment \"sharpness\" on held item.",
        "Items read back from saved variables have no enchantments. If the value is not an item the line stops with a \"there is no item\" error."})
@Examples({"on key press of \"e\":",
        "\tif level of enchantment \"efficiency\" on held item is less than 5:",
        "\t\tsend \"not a max efficiency tool\"",
        "",
        "on item tooltip:",
        "\tset {_mending} to enchantment level of \"mending\" on event-item",
        "\tif {_mending} is 1:",
        "\t\tadd \"&amending\" to the tooltip",
        "",
        "on key press of \"e\":",
        "\tsend \"sharpness %held item's sharpness level%, looting %level of looting of held item%\""})
@Since({"1.0.0-alpha.9", "1.0.0-alpha.11"})
public final class ExprEnchantmentLevel implements Expression {
    private static final String LEVEL = "[(enchantment|enchant)] (level|levels)";

    private final Expression enchantment;
    private final Expression item;

    private ExprEnchantmentLevel(Expression enchantment, Expression item) {
        this.enchantment = enchantment;
        this.item = item;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, ExprEnchantmentLevel::create,
                "[the] level of enchantment %string% on %item%",
                "[the] enchantment level of %string% on %item%",
                "[the] " + LEVEL + " of %enchantment% (on|of) %item%",
                "[the] %enchantment% " + LEVEL + " (on|of) %item%",
                "%item%'s %enchantment% " + LEVEL,
                "%item%'s " + LEVEL + " of %enchantment%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        boolean itemFirst = match.patternIndex() >= 4;
        Expression enchantment = match.slot(itemFirst ? 1 : 0);
        Expression item = match.slot(itemFirst ? 0 : 1);
        if (enchantment.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprEnchantmentLevel(enchantment, item));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        Object value = enchantment.evaluate(context);
        String id = value instanceof Enchantment found
                ? found.id()
                : Enchantments.id(Converters.toText(value, context));
        if (!(item.evaluate(context) instanceof ItemValue found)) {
            throw new ScriptError("there is no item");
        }
        return (double) found.details().enchantments().getOrDefault(id, 0);
    }
}
