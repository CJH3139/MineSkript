package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Items In Inventory")
@Description("Every item stack in an inventory, like Skript's items in: one entry per slot that is not empty, in slot order. For your own inventory that is the hotbar, storage, armour and offhand; for the current inventory every slot of the open screen. Loop it to look at each stack, or count the stacks with size of.")
@Examples({
        "on key press of \"l\":",
        "	loop all items in player's inventory:",
        "		send \"%loop-value%\"",
        "",
        "on inventory open:",
        "	send \"%size of items in current inventory% stacks in here\""
})
@Since("1.0.0-alpha.17")
public final class ExprItemsIn implements Expression {
    private final Expression inventory;

    private ExprItemsIn(Expression inventory) {
        this.inventory = inventory;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ITEM, Tier.COMBINED, Priority.before(Priority.SIMPLE),
                (match, scope) -> Optional.of(new ExprItemsIn(match.slot(0))),
                "[all [[of] the]] items (in|within|of|contained in|out of) [inventory] %inventory%");
    }

    @Override
    public SkType type() {
        return SkType.ITEM;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public Object evaluate(Context context) {
        List<ItemValue> items = Inventories.items(context.world(), Inventories.inventory(inventory, context));
        return items.stream().filter(item -> !item.isEmpty()).toList();
    }
}
