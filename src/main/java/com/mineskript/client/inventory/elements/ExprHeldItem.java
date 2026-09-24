package com.mineskript.client.inventory.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Held Item")
@Description("The item stack in your main hand (the selected hotbar slot). Also written item in hand or tool. An empty hand gives air (a count of 0), not none, so check it with is air. Printed in text an item shows its display name, with the count in front when there is more than one, like 32 Cobblestone. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"x\":",
        "\tsend \"holding %held item%\"",
        "",
        "on key press of \"x\":",
        "\tif held item is air:",
        "\t\tsend \"empty hand\"",
        "\telse:",
        "\t\tsend \"%count of held item% of %id of held item%\""
})
@Since("1.0.0-alpha.2")
public final class ExprHeldItem extends GameValueExpression {
    private ExprHeldItem() {
        super(SkType.ITEM, GameBridge::heldItem);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ITEM, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprHeldItem()), "[the] (held item|item in hand|tool)");
    }
}
