package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.InventoryRef;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Can Hold")
@Description({
        "Checks whether an inventory has room for an item, like Skript's can hold: player can hold 64 cobblestone is true when the empty slots and the unfilled stacks of cobblestone together fit 64 more. In your own inventory only the 36 hotbar and storage slots count; in the current inventory every slot of the open screen, whatever the screen would really accept there.",
        "The amount goes in front as in has: player can hold 3 diamonds, and without one it is a single item. With several items joined by and each must fit on its own; joined by or, one is enough. Also written has space for or has enough space to hold, and negated with can't hold or cannot hold."
})
@Examples({
        "on key press of \"m\":",
        "	if player can't hold 64 cobblestone:",
        "		send \"make room before mining\"",
        "",
        "on inventory open:",
        "	if current inventory has enough space for 1 diamond:",
        "		send \"this chest has room\""
})
@Since("1.0.0-alpha.17")
public final class CondCanHold implements Condition {
    private static final String CAN = "%inventories% (can hold|(has|have) [enough] space (for|to hold)) ";
    private static final String CANNOT = "%inventories% (can't hold|cannot hold|can not hold|(doesn't|does not|don't|do not) have [enough] space (for|to hold)) ";
    private static final String ITEMS = "%itemtypes%";
    private static final String AMOUNT = "%number% [of] %itemtypes%";

    private final Expression inventory;
    private final Expression amount;
    private final Expression items;
    private final boolean negate;

    private CondCanHold(Expression inventory, Expression amount, Expression items, boolean negate) {
        this.inventory = inventory;
        this.amount = amount;
        this.items = items;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.before(Priority.COMBINED), CondCanHold::create,
                CAN + ITEMS,
                CAN + AMOUNT,
                CANNOT + ITEMS,
                CANNOT + AMOUNT);
    }

    private static Optional<Condition> create(Match match, ParseScope scope) {
        int index = match.patternIndex();
        boolean counted = index % 2 == 1;
        Expression amount = counted ? match.slot(1) : null;
        if (counted && !(amount instanceof ConstantExpression)) {
            return Optional.empty();
        }
        return Optional.of(new CondCanHold(match.slot(0), amount, counted ? match.slot(2) : match.slot(1), index >= 2));
    }

    @Override
    public boolean test(Context context) {
        int needed = amount == null ? 1 : (int) Math.ceil((Double) amount.evaluate(context));
        Object value = items.evaluate(context);
        List<?> wanted = value instanceof List<?> list ? list : List.of(value);
        GameBridge game = context.world();
        List<ItemValue> slots = Inventories.storage(game, Inventories.inventory(inventory, context));
        boolean any = items instanceof ListExpression list && list.disjunctive();
        boolean result = any
                ? wanted.stream().anyMatch(item -> fits(item, needed, slots, game))
                : !wanted.isEmpty() && wanted.stream().allMatch(item -> fits(item, needed, slots, game));
        return negate != result;
    }

    private static boolean fits(Object item, int needed, List<ItemValue> slots, GameBridge game) {
        if (!(item instanceof BlockType type)) {
            return false;
        }
        String id = Inventories.id(game, type);
        int stack = game.maxStackSize(id);
        long room = 0;
        for (ItemValue slot : slots) {
            if (slot.isEmpty()) {
                room += stack;
            } else if (slot.id().equals(id)) {
                room += Math.max(0, stack - slot.count());
            }
        }
        return room >= needed;
    }
}
