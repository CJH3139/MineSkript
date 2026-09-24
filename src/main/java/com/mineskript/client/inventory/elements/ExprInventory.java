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

@Name("Inventory")
@Description({
        "Your inventory, like Skript's inventory of player: the hotbar, storage, armour and offhand slots. Written inventory of player or player's inventory. Use it where an inventory is expected, such as player's inventory has 3 diamonds, amount of stone in player's inventory, slot 0 of player's inventory or player's inventory is full.",
        "MineSkript runs on your own client, so this is always your inventory. Where an inventory is expected, player works on its own too, as in player has diamond. Printed in text it shows inventory of and your name. It is not saved in variables."
})
@Examples({
        "on key press of \"i\":",
        "	if player's inventory has 3 diamonds:",
        "		send \"enough diamonds\"",
        "",
        "on key press of \"i\":",
        "	send \"%amount of cobblestone in inventory of player% cobblestone\""
})
@Since("1.0.0-alpha.11")
public final class ExprInventory implements Expression {
    private ExprInventory() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.INVENTORY, Tier.PROPERTY, (match, scope) -> Optional.of(new ExprInventory()),
                "[the] (inventory|inventories) of %players%",
                "%players%'s (inventory|inventories)");
    }

    @Override
    public SkType type() {
        return SkType.INVENTORY;
    }

    @Override
    public Object evaluate(Context context) {
        return InventoryRef.LOCAL;
    }
}
