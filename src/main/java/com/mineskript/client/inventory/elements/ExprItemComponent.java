package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Locale;
import java.util.Optional;

@Name("Item Data Component")
@Description({"Reads any piece of an item's data, which Minecraft keeps in data components (they replaced NBT in 1.20.5). Give the component id as text, such as \"minecraft:custom_data\" (the data servers attach to their items), \"minecraft:rarity\" or \"minecraft:max_stack_size\"; without a namespace minecraft: is assumed. The value comes back as text: plain values such as epic or 64 as they are, and compound values in Minecraft's text format (SNBT), such as {id:\"sword\",tier:3}.",
        "Gives none when the item does not have that component or the id is not a component. Items read back from saved variables have no components. If the value is not an item the line stops with a \"there is no item\" error.",
        "Only data your game already received is read; nothing is asked from the server."})
@Examples({"on key press of \"c\":",
        "\tset {_data} to component \"custom_data\" of held item",
        "\tif {_data} is set:",
        "\t\tsend \"custom data: %{_data}%\"",
        "",
        "on item tooltip:",
        "\tif data component \"rarity\" of event-item is \"epic\":",
        "\t\tadd \"&5&lepic!\" to the tooltip"})
@Since("1.0.0-alpha.9")
public final class ExprItemComponent implements Expression {
    private final Expression id;
    private final Expression item;

    private ExprItemComponent(Expression id, Expression item) {
        this.id = id;
        this.item = item;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY,
                (match, scope) -> match.slot(0).isList() ? Optional.empty()
                        : Optional.of(new ExprItemComponent(match.slot(0), match.slot(1))),
                "[the] [data] component %string% of %item%");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        String name = ((String) id.evaluate(context)).trim().toLowerCase(Locale.ROOT);
        if (!(item.evaluate(context) instanceof ItemValue found)) {
            throw new ScriptError("there is no item");
        }
        String key = name.contains(":") ? name : "minecraft:" + name;
        return found.details().component(key).<Object>map(text -> text).orElse(None.NONE);
    }
}
