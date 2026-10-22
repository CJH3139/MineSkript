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
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Click Slot")
@Description({
        "Clicks an inventory slot as if you did it with the mouse, so the game and the server see a normal click. click slot 5 left-clicks: it picks up the stack in the slot, or puts down what is on the cursor. right click takes half or puts down one, shift click moves the stack to the other inventory, middle click copies a stack in creative, and double click gathers every matching item onto the cursor.",
        "Slots are numbered like slot %number% of an inventory: in your own inventory 0 to 8 are the hotbar, 9 to 35 the storage, 36 to 39 armour and 40 the offhand, and in the current inventory they count from 0. Without of, it is your own inventory. Your inventory does not need to be open; with a chest open, slots of your inventory that the chest screen does not show, such as armour, are left alone. A click on a slot that does not exist does nothing.",
        "MineSkript cannot set items directly, since the server decides what is in your inventory; clicking is how scripts move items. Servers may treat very fast clicking as cheating, so wait a tick or two between clicks."
})
@Examples({
        "on key press of \"r\":",
        "	shift click slot 0 of current inventory",
        "",
        "on inventory open:",
        "	loop 27 times:",
        "		shift click slot (loop-iteration - 1) of the current inventory",
        "		wait 2 ticks",
        "",
        "on key press of \"y\":",
        "	click slot 9",
        "	wait 1 tick",
        "	right click slot 10"
})
@Since("1.0.0-alpha.17")
public final class EffClickSlot implements Statement {
    private static final String SLOT = " [on] slot %number% [(of|in) %inventory%]";
    private static final List<InventoryClick> CLICKS = List.of(
            InventoryClick.left(),
            InventoryClick.right(),
            InventoryClick.shift(),
            new InventoryClick(InventoryClick.Kind.QUICK_MOVE, 1),
            new InventoryClick(InventoryClick.Kind.CLONE, 2),
            new InventoryClick(InventoryClick.Kind.PICKUP_ALL, 0));

    private final int line;
    private final Expression slot;
    private final Expression inventory;
    private final InventoryClick click;

    private EffClickSlot(int line, Expression slot, Expression inventory, InventoryClick click) {
        this.line = line;
        this.slot = slot;
        this.inventory = inventory;
        this.click = click;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.before(Priority.SIMPLE), EffClickSlot::create,
                "[left] click" + SLOT,
                "right click" + SLOT,
                "shift [left] click" + SLOT,
                "shift right click" + SLOT,
                "middle click" + SLOT,
                "double click" + SLOT);
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffClickSlot(scope.line(), match.slot(0), match.slot(1), CLICKS.get(match.patternIndex())));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().clickSlot(Inventories.inventory(inventory, context), Inventories.slot(slot, context), click);
        return Flow.CONTINUE;
    }
}
