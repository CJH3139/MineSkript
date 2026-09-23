package com.mineskript.syntax.conditions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondInventory implements Condition {
    private static final String STATES = "(full:full|empty:empty)";

    private final Expression slot;
    private final boolean full;
    private final boolean negate;

    private CondInventory(Expression slot, boolean full, boolean negate) {
        this.slot = slot;
        this.full = full;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, false), "[the] inventory (is|are) " + STATES);
        registry.addCondition((match, scope) -> create(match, true),
                "[the] inventory (isn't|is not|aren't|are not) " + STATES);
        registry.addCondition((match, scope) -> slotCondition(match, false), "slot %number% (is|are) empty");
        registry.addCondition((match, scope) -> slotCondition(match, true),
                "slot %number% (isn't|is not|aren't|are not) empty");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        return Optional.of(new CondInventory(null, match.has("full"), negate));
    }

    private static Optional<Condition> slotCondition(Match match, boolean negate) {
        Expression slot = match.slot(0);
        if (slot.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondInventory(slot, false, negate));
    }

    @Override
    public boolean test(Context context) {
        GameBridge game = context.world();
        boolean result;
        if (slot != null) {
            int index = (int) Math.round((Double) slot.evaluate(context));
            result = game.itemInSlot(index).isEmpty();
        } else if (full) {
            result = game.freeSlots() == 0;
        } else {
            result = game.freeSlots() == game.inventorySize();
        }
        return negate != result;
    }
}
