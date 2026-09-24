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

@Name("Item Lore")
@Description({"The lore of an item: the extra lines of text under its name that a server or a command gave it, as a list of plain text lines without colours, top to bottom. An item without lore gives an empty list.",
        "It does not include lines the game adds by itself (enchantments, attributes) or lines scripts add with add to tooltip. Items read back from saved variables have no lore. If the value is not an item the line stops with a \"there is no item\" error."})
@Examples({"on key press of \"l\":",
        "\tloop lore of held item:",
        "\t\tsend \"lore: %loop-value%\"",
        "",
        "on inventory change:",
        "\tif lore of event-item contains \"Soulbound\":",
        "\t\tsend \"soulbound item picked up\""})
@Since("1.0.0-alpha.9")
public final class ExprItemLore extends ItemPropertyExpression {
    private ExprItemLore(Expression item) {
        super(SkType.TEXT, value -> value.details().lore(), item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemLore(match.slot(0))),
                "[the] lore of %item%",
                "%item%'s lore");
    }

    @Override
    public boolean isList() {
        return true;
    }
}
