package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.game.InventoryClick;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.InventoryRef;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Move Slot")
@Description({
        "Moves the stack in one slot to another slot with clicks, the way you would by hand: it picks up the first slot and puts it down in the second. If the second slot held something different, the two swap places; if it held the same item, they stack together and anything left over goes back.",
        "Each slot is numbered like slot %number% of an inventory, and without of it is your own inventory, which does not need to be open. Both slots must be on the open screen. The cursor should be empty when it starts; whatever it carries is put down by the first click."
})
@Examples({
        "on key press of \"n\":",
        "	move the stack in slot 9 to slot 0",
        "",
        "on inventory open:",
        "	move item from slot 0 of current inventory to slot 35 of player's inventory"
})
@Since("1.0.0-alpha.17")
public final class EffMoveSlot implements Statement {
    private static final String FROM = "slot %number% [(of|in) %inventory%]";

    private final int line;
    private final Expression from;
    private final Expression fromInventory;
    private final Expression to;
    private final Expression toInventory;

    private EffMoveSlot(int line, Expression from, Expression fromInventory, Expression to, Expression toInventory) {
        this.line = line;
        this.from = from;
        this.fromInventory = fromInventory;
        this.to = to;
        this.toInventory = toInventory;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffMoveSlot::create,
                "move [the] (item|items|stack) (in|from) " + FROM + " to " + FROM);
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(2).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffMoveSlot(scope.line(), match.slot(0), match.slot(1), match.slot(2), match.slot(3)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        InventoryRef source = Inventories.inventory(fromInventory, context);
        int sourceSlot = Inventories.slot(from, context);
        if (!game.clickSlot(source, sourceSlot, InventoryClick.left())) {
            return Flow.CONTINUE;
        }
        game.clickSlot(Inventories.inventory(toInventory, context), Inventories.slot(to, context), InventoryClick.left());
        if (!game.cursorItem().isEmpty()) {
            game.clickSlot(source, sourceSlot, InventoryClick.left());
        }
        return Flow.CONTINUE;
    }
}
