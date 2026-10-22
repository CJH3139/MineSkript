package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.InventoryClick;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Swap Slot With Hotbar")
@Description("Swaps an inventory slot with a hotbar slot or the offhand in one move, like pressing a number key (or F) while hovering over the slot. Slots are numbered like slot %number% of an inventory, and without of it is your own inventory, which does not need to be open. The hotbar slot is 0 to 8; any other number does nothing.")
@Examples({
        "on key press of \"h\":",
        "	swap slot 0 of current inventory with hotbar slot 0",
        "",
        "on key press of \"t\":",
        "	swap slot 9 with the offhand"
})
@Since("1.0.0-alpha.17")
public final class EffSwapSlot implements Statement {
    private static final int HOTBAR_SLOTS = 9;
    private static final String SLOT = "swap slot %number% [(of|in) %inventory%] with [the] ";

    private final int line;
    private final Expression slot;
    private final Expression inventory;
    private final Expression hotbar;

    private EffSwapSlot(int line, Expression slot, Expression inventory, Expression hotbar) {
        this.line = line;
        this.slot = slot;
        this.inventory = inventory;
        this.hotbar = hotbar;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffSwapSlot::create,
                SLOT + "hotbar slot %number%",
                SLOT + "(offhand|off hand) [slot]");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffSwapSlot(scope.line(), match.slot(0), match.slot(1),
                match.patternIndex() == 0 ? match.slot(2) : null));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        int button = InventoryClick.OFFHAND_BUTTON;
        if (hotbar != null) {
            button = Inventories.slot(hotbar, context);
            if (button < 0 || button >= HOTBAR_SLOTS) {
                return Flow.CONTINUE;
            }
        }
        context.world().clickSlot(Inventories.inventory(inventory, context), Inventories.slot(slot, context),
                new InventoryClick(InventoryClick.Kind.SWAP, button));
        return Flow.CONTINUE;
    }
}
