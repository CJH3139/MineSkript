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
import java.util.Optional;

@Name("Item Count In Inventory")
@Description({
        "The total number of a given item across your whole inventory, adding up every stack. The item is written as a plain name such as stone, diamond or oak log (words become an id like minecraft:oak_log). Counts the hotbar, storage, armor and offhand slots, and returns 0 when you have none. Needs a world: outside a world the line stops with a \"no world\" error.",
        "The name must match the item id exactly, so use the id form (for example oak_planks, not planks). Different enchantments or names on the same item still count together."
})
@Examples({
        "on key press of \"c\":",
        "\tsend \"you have %number of diamond in inventory% diamonds\"",
        "",
        "on inventory change:",
        "\tif number of cobblestone in the inventory is at least 64:",
        "\t\tsend \"a full stack of cobblestone\""
})
@Since("1.0.0-alpha.2")
public final class ExprInventoryCount implements Expression {
    private final Expression type;

    private ExprInventoryCount(Expression type) {
        this.type = type;
    }

    public static void register(SyntaxRegistry registry) {
        // Before the list size ("number of %objects%") and the other combined expressions.
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, Priority.before(Priority.SIMPLE),
                (match, scope) -> Optional.of(new ExprInventoryCount(match.slot(0))),
                "[the] number of %blocktype% in [the] inventory");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().countItem(((BlockType) type.evaluate(context)).id());
    }
}
