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
@Description("The item stack in a numbered inventory slot. Slots 0 to 8 are the hotbar, 9 to 35 the storage rows, 36 to 39 armor (boots, leggings, chestplate, helmet) and 40 the offhand. The number is rounded; empty slots and numbers outside 0 to 40 give air. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"i\":",
        "\tsend \"first hotbar slot: %item in slot 0%\"",
        "",
        "on key press of \"i\":",
        "\tloop 9 times:",
        "\t\tset {_slot} to loop-iteration - 1",
        "\t\tsend \"slot %{_slot}%: %item in slot {_slot}%\""
})
@Since("1.0.0-alpha.2")
public final class ExprItemInSlot implements Expression {
    private final Expression slot;

    private ExprItemInSlot(Expression slot) {
        this.slot = slot;
    }

    public static void register(SyntaxRegistry registry) {
        // Before the text and maths expressions, so item in slot {_s} parsed as number takes its slot number from
        // the text.
        registry.addExpression(SkType.ITEM, Tier.COMBINED, Priority.before(Priority.SIMPLE),
                (match, scope) -> Optional.of(new ExprItemInSlot(match.slot(0))),
                "[the] item in slot %number%");
    }

    @Override
    public SkType type() {
        return SkType.ITEM;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().itemInSlot((int) Math.round((Double) slot.evaluate(context)));
    }
}
