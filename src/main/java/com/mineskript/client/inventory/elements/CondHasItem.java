package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Has Item")
@Description({
        "Checks whether your inventory holds at least one of an item, anywhere: hotbar, main inventory, armour slots and offhand. Items are written as block type words like diamond, golden apple or minecraft:ender_pearl. It always reads your own inventory.",
        "An item name is up to four words. Words the language uses itself, such as of, on, in or the, cannot appear in it, so write those names with underscores: totem_of_undying."
})
@Examples({
        "on key press of \"p\":",
        "	if player has ender pearl:",
        "		send \"%number of ender pearl in inventory% pearls left\"",
        "	else:",
        "		send \"out of pearls\"",
        "",
        "every 10 seconds:",
        "	if player does not have cooked beef:",
        "		show action bar \"no food left\""
})
@Since("1.0.0-alpha.2")
public final class CondHasItem implements Condition {
    private final Expression item;
    private final boolean negate;

    private CondHasItem(Expression item, boolean negate) {
        this.item = item;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.COMBINED, (match, scope) -> create(match, match.patternIndex() == 1),
                "%player% (has|have) %blocktype%",
                "%player% (doesn't have|does not have|don't have|do not have) %blocktype%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression item = match.slot(1);
        if (item.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondHasItem(item, negate));
    }

    @Override
    public boolean test(Context context) {
        Object value = item.evaluate(context);
        boolean result = false;
        if (value instanceof BlockType type) {
            result = context.world().countItem(type.id()) > 0;
        }
        return negate != result;
    }
}
