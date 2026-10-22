package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Item In Slot")
@Description({
        "The item stack in a numbered inventory slot. Slots 0 to 8 are the hotbar, 9 to 35 the storage rows, 36 to 39 armor (boots, leggings, chestplate, helmet) and 40 the offhand. The number is rounded; empty slots and numbers outside 0 to 40 give air. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Like Skript's inventory slot it can also be written slot 0 of player's inventory, slot 0 of player or player's inventory's slot 0. MineSkript gives the item in the slot, so it can be read but not set; use the click effects to move items.",
        "slot 0 of current inventory reads the open screen instead, such as a chest, numbered from 0 like Skript's: a chest's 27 slots are 0 to 26. Numbers past the last slot give air."
})
@Examples({
        "on key press of \"i\":",
        "	send \"first hotbar slot: %item in slot 0%\"",
        "",
        "on key press of \"i\":",
        "	loop 9 times:",
        "		set {_slot} to loop-iteration - 1",
        "		send \"slot %{_slot}%: %item in slot {_slot}%\"",
        "",
        "on key press of \"i\":",
        "	if slot 40 of player's inventory is shield:",
        "		send \"shield ready\"",
        "",
        "on inventory open:",
        "	send \"the first chest slot holds %slot 0 of current inventory%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.17"})
public final class ExprItemInSlot implements Expression {
    private final Expression slot;
    private final Expression inventory;

    private ExprItemInSlot(Expression slot, Expression inventory) {
        this.slot = slot;
        this.inventory = inventory;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ITEM, Tier.COMBINED, Priority.before(Priority.SIMPLE),
                (match, scope) -> Optional.of(switch (match.patternIndex()) {
                    case 0 -> new ExprItemInSlot(match.slot(0), null);
                    case 1 -> new ExprItemInSlot(match.slot(0), match.slot(1));
                    default -> new ExprItemInSlot(match.slot(1), match.slot(0));
                }),
                "[the] item in slot %number%",
                "[the] (slot|slots) %number% of %inventory%",
                "%inventory%'s (slot|slots) %number%");
    }

    @Override
    public SkType type() {
        return SkType.ITEM;
    }

    @Override
    public Object evaluate(Context context) {
        return Inventories.item(context.world(), Inventories.inventory(inventory, context), Inventories.slot(slot, context));
    }
}
