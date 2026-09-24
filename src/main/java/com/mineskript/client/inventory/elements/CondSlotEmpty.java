package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Slot Is Empty")
@Description("Checks whether one inventory slot holds nothing. Slots are numbered from 0: 0 to 8 are the hotbar left to right, 9 to 35 the main inventory, 36 to 39 armour and 40 the offhand. Decimal slot numbers are rounded, and a slot number outside 0 to 40 always counts as empty.")
@Examples({
        "on key press of \"x\":",
        "	if slot 8 is empty:",
        "		send \"last hotbar slot is free\"",
        "	else:",
        "		select slot 8",
        "",
        "on key press of \"o\":",
        "	if slot 40 is not empty:",
        "		send \"offhand holds %offhand item%\""
})
@Since("1.0.0-alpha.2")
public final class CondSlotEmpty implements Condition {
    private final Expression slot;
    private final boolean negate;

    private CondSlotEmpty(Expression slot, boolean negate) {
        this.slot = slot;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "slot %number% (is|are) empty",
                "slot %number% (isn't|is not|aren't|are not) empty");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression slot = match.slot(0);
        if (slot.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondSlotEmpty(slot, negate));
    }

    @Override
    public boolean test(Context context) {
        int index = (int) Math.round((Double) slot.evaluate(context));
        return negate != context.world().itemInSlot(index).isEmpty();
    }
}
