package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.InventoryRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Current Inventory")
@Description({
        "The inventory of the screen you have open, like Skript's current inventory of player: a chest, a furnace, a crafting table, a villager's trades and so on. Its slots are numbered from 0 in the order the screen shows them, so a chest's are 0 to 26 (0 to 53 for a double chest), and your own inventory below them is not part of it. Also written open inventory, opened inventory or top inventory.",
        "With no screen open, or with your own inventory open, it is your 2x2 crafting area: slot 0 is the result and 1 to 4 the grid, as in Skript. Use it where an inventory is expected: slot 4 of current inventory, current inventory has diamond, amount of stone in current inventory, rows of current inventory, or click slot 4 of current inventory."
})
@Examples({
        "on inventory open:",
        "	if current inventory has diamond:",
        "		send \"this chest has diamonds\"",
        "",
        "on key press of \"k\":",
        "	send \"%amount of iron ingot in the current inventory% iron in here\""
})
@Since("1.0.0-alpha.17")
public final class ExprOpenedInventory implements Expression {
    private ExprOpenedInventory() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.INVENTORY, Tier.PROPERTY, (match, scope) -> Optional.of(new ExprOpenedInventory()),
                "[the] (current|open|opened|top) (inventory|inventories) [of %players%]",
                "%players%'s (current|open|opened|top) (inventory|inventories)");
    }

    @Override
    public SkType type() {
        return SkType.INVENTORY;
    }

    @Override
    public Object evaluate(Context context) {
        return InventoryRef.OPEN;
    }
}
