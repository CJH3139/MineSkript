package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Inventory Is Full / Empty")
@Description("Checks whether your main inventory is full (no free slot) or empty (every slot free). Only the 36 main slots count, the hotbar included; armour and the offhand are ignored, so the inventory can be empty while you wear armour. A slot with a partly filled stack is not free, so full does not mean every stack is at its maximum.")
@Examples({
        "on inventory change:",
        "	if inventory is full:",
        "		show title \"inventory full\"",
        "",
        "on key press of \"i\":",
        "	if the inventory is empty:",
        "		send \"nothing to sort\"",
        "",
        "on key press of \"r\":",
        "	if inventory is not full:",
        "		hold attack",
        "		wait 5 seconds",
        "		release attack"
})
@Since("1.0.0-alpha.2")
public final class CondInventoryFull implements Condition {
    private static final String STATES = "(full:full|empty:empty)";

    private final boolean full;
    private final boolean negate;

    private CondInventoryFull(boolean full, boolean negate) {
        this.full = full;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "[the] inventory (is|are) " + STATES,
                "[the] inventory (isn't|is not|aren't|are not) " + STATES);
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        return Optional.of(new CondInventoryFull(match.has("full"), negate));
    }

    @Override
    public boolean test(Context context) {
        GameBridge game = context.world();
        boolean result;
        if (full) {
            result = game.freeSlots() == 0;
        } else {
            result = game.freeSlots() == game.inventorySize();
        }
        return negate != result;
    }
}
