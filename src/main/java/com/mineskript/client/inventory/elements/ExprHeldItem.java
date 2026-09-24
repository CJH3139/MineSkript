package com.mineskript.client.inventory.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.PastState;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Held Item")
@Description("The item stack in your main hand (the selected hotbar slot). Also written item in hand, and like Skript tool, weapon, tool of player or player's held item. In on held item change, past held item (or former tool) is the item in the slot you switched away from (see Former State). An empty hand gives air (a count of 0), not none, so check it with is air. Printed in text an item shows its display name, with the count in front when there is more than one, like 32 Cobblestone. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"x\":",
        "\tsend \"holding %held item%\"",
        "",
        "on key press of \"x\":",
        "\tif held item is air:",
        "\t\tsend \"empty hand\"",
        "\telse:",
        "\t\tsend \"%count of held item% of %id of held item%\"",
        "",
        "on key press of \"t\":",
        "\tsend \"your tool: %player's tool%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprHeldItem extends GameValueExpression implements PastState {
    private ExprHeldItem() {
        super(SkType.ITEM, GameBridge::heldItem);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ITEM, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprHeldItem()),
                "[the] item in hand",
                "[the] (held item|tool|weapon) [of %players%]",
                "%players%'s (held item|tool|weapon)");
    }

    @Override
    public String pastEventValue() {
        return "previous item";
    }
}
