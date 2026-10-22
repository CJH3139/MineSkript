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
import java.util.List;
import java.util.Optional;

@Name("First Empty Slot")
@Description("The number of the first empty slot in an inventory, like Skript's first empty slot. In your own inventory only the 36 hotbar and storage slots are searched, hotbar first; in the current inventory every slot of the open screen. When there is no empty slot it is not set, so check it with is set.")
@Examples({
        "on key press of \"j\":",
        "	if first empty slot of player's inventory is set:",
        "		send \"slot %first empty slot of player's inventory% is free\"",
        "",
        "on inventory open:",
        "	if first empty slot in current inventory is not set:",
        "		send \"this chest is full\""
})
@Since("1.0.0-alpha.17")
public final class ExprFirstEmptySlot implements Expression {
    private final Expression inventory;

    private ExprFirstEmptySlot(Expression inventory) {
        this.inventory = inventory;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED,
                (match, scope) -> Optional.of(new ExprFirstEmptySlot(match.slot(0))),
                "[the] first empty slot [index] (of|in) %inventory%",
                "%inventory%'s first empty slot [index]");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        List<ItemValue> items = Inventories.storage(context.world(), Inventories.inventory(inventory, context));
        for (int slot = 0; slot < items.size(); slot++) {
            if (items.get(slot).isEmpty()) {
                return (double) slot;
            }
        }
        return None.NONE;
    }
}
