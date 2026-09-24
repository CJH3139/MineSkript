package com.mineskript.client.inventory.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Used Slots")
@Description("How many of the 36 main inventory slots (hotbar plus storage) hold an item, as a whole number. It is always 36 minus free slots; armor and offhand are not counted. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"u\":",
        "\tsend \"%used slots% used, %free slots% free\""
})
@Since("1.0.0-alpha.2")
public final class ExprUsedSlots extends GameValueExpression {
    private ExprUsedSlots() {
        super(SkType.NUMBER, game -> (double) (game.inventorySize() - game.freeSlots()));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprUsedSlots()), "[the] used slots");
    }
}
