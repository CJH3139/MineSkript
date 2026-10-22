package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.game.InventoryClick;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.InventoryRef;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Name("Quick Move Items")
@Description({
        "Shift-clicks every stack of some items in an inventory, so they move to the other side of the open screen: from a chest into your inventory, or from your inventory into the chest. quick move all diamonds in the current inventory takes every diamond out of a chest; quick move everything in player's inventory puts all you carry into it. Also written shift click.",
        "In your own inventory only the 36 hotbar and storage slots are moved, never armour or the offhand. With no other inventory open, shift-clicking moves stacks between the hotbar and the storage. All the clicks happen at once; servers that limit fast clicking may ignore some, so loop with a wait instead if that happens."
})
@Examples({
        "on inventory open:",
        "	if event-screen title contains \"Chest\":",
        "		quick move all diamonds and emeralds in the current inventory",
        "",
        "on key press of \"x\":",
        "	shift click everything in player's inventory"
})
@Since("1.0.0-alpha.17")
public final class EffQuickMove implements Statement {
    private static final String MOVE = "(quick move|shift click) ";
    private static final String IN = " (in|from|out of) %inventory%";

    private final int line;
    private final Expression items;
    private final Expression inventory;

    private EffQuickMove(int line, Expression items, Expression inventory) {
        this.line = line;
        this.items = items;
        this.inventory = inventory;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffQuickMove::create,
                MOVE + "(everything|all items|all [of] the items)" + IN,
                MOVE + "[all [[of] the]] %itemtypes%" + IN);
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        boolean everything = match.patternIndex() == 0;
        return Optional.of(new EffQuickMove(scope.line(), everything ? null : match.slot(0),
                everything ? match.slot(0) : match.slot(1)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        InventoryRef where = Inventories.inventory(inventory, context);
        Set<String> ids = items == null ? null : ids(game, items.evaluate(context));
        List<ItemValue> slots = Inventories.storage(game, where);
        for (int slot = 0; slot < slots.size(); slot++) {
            ItemValue item = slots.get(slot);
            if (!item.isEmpty() && (ids == null || ids.contains(item.id()))) {
                game.clickSlot(where, slot, InventoryClick.shift());
            }
        }
        return Flow.CONTINUE;
    }

    private static Set<String> ids(GameBridge game, Object value) {
        Set<String> ids = new HashSet<>();
        for (Object item : value instanceof List<?> list ? list : List.of(value)) {
            if (item instanceof BlockType type) {
                ids.add(Inventories.id(game, type));
            }
        }
        return ids;
    }
}
