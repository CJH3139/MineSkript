package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Item Count In Inventory")
@Description({
        "The total number of a given item across your whole inventory, adding up every stack. The item is written as a plain name such as stone, diamond or oak log (words become an id like minecraft:oak_log). Counts the hotbar, storage, armor and offhand slots, and returns 0 when you have none. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Like Skript it can be written amount of stone in player's inventory or number of diamonds of inventory of player; with several items (diamond and emerald) their counts are added up. The name must match the item id, so use the id form (for example oak_planks, not planks); a name ending in s is also tried without it, so diamonds counts diamond. Different enchantments or names on the same item still count together."
})
@Examples({
        "on key press of \"c\":",
        "	send \"you have %number of diamond in inventory% diamonds\"",
        "",
        "on inventory change:",
        "	if number of cobblestone in the inventory is at least 64:",
        "		send \"a full stack of cobblestone\"",
        "",
        "on key press of \"c\":",
        "	send \"%amount of iron ingot in player's inventory% iron\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprInventoryCount implements Expression {
    private final Expression type;

    private ExprInventoryCount(Expression type) {
        this.type = type;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, Priority.before(Priority.SIMPLE),
                (match, scope) -> Optional.of(new ExprInventoryCount(match.slot(0))),
                "[the] number of %itemtype% in [the] inventory",
                "[the] (amount|number) of %itemtypes% (in|of) %inventories%");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        Object value = type.evaluate(context);
        List<?> types = value instanceof List<?> list ? list : List.of(value);
        int total = 0;
        for (Object item : types) {
            total += Inventories.count(context.world(), (BlockType) item);
        }
        return (double) total;
    }
}
