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

@Name("Free Slots")
@Description("How many of the 36 main inventory slots (hotbar plus the 27 storage slots) are empty, as a whole number. Armor and offhand slots are not counted. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on inventory change:",
        "\tif free slots is less than 3:",
        "\t\tshow action bar \"inventory almost full\""
})
@Since("1.0.0-alpha.2")
public final class ExprFreeSlots extends GameValueExpression {
    private ExprFreeSlots() {
        super(SkType.NUMBER, game -> (double) game.freeSlots());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprFreeSlots()), "[the] free slots");
    }
}
