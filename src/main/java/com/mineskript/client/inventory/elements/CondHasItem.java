package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Name("Has Item")
@Description({
        "Checks whether your inventory holds an item, anywhere: hotbar, main inventory, armour slots and offhand. Items are written as item type words like diamond, golden apple or minecraft:ender_pearl. It always reads your own inventory.",
        "Like Skript, an amount can go in front: player has 3 diamonds (or 64 of stone) is true when you have at least that many in total. The amount is written as a number, as in Skript's item types. A name ending in s is also tried without it, so diamonds and torches count diamond and torch. With several items joined by and you need all of them; joined by or, one is enough. One amount counts for every item in the list, as in player has 2 of diamond and emerald. It can also be written player's inventory has stone, player has stone in their inventory, or player's inventory contains stone.",
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
        "		show action bar \"no food left\"",
        "",
        "on key press of \"c\":",
        "	if player has 3 diamonds:",
        "		send \"enough for a pickaxe\"",
        "",
        "on key press of \"c\":",
        "	if player has diamond and emerald:",
        "		send \"ready to trade\"",
        "",
        "on key press of \"c\":",
        "	if player's inventory doesn't contain 64 of cobblestone:",
        "		send \"mine some more\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class CondHasItem implements Condition {
    private static final String HOLDER = "%players/inventories% ";
    private static final String IN = " [in [(the|their|his|her|its)] inventory]";
    private static final String HAVE = "(has|have) ";
    private static final String NOT_HAVE = "(doesn't have|does not have|don't have|do not have) ";
    private static final String CONTAIN = "%inventories% (contains|contain) ";
    private static final String NOT_CONTAIN = "%inventories% (doesn't|does not|do not|don't) contain ";
    private static final String ITEMS = "%itemtypes%";
    private static final String AMOUNT = "%number% [of] %itemtypes%";
    private static final Set<Integer> NEGATED = Set.of(2, 3, 6, 7);
    private static final int FIRST_CONTAINS = 4;

    private final Expression amount;
    private final Expression items;
    private final boolean negate;

    private CondHasItem(Expression amount, Expression items, boolean negate) {
        this.amount = amount;
        this.items = items;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.COMBINED, CondHasItem::create,
                HOLDER + HAVE + ITEMS + IN,
                HOLDER + HAVE + AMOUNT + IN,
                HOLDER + NOT_HAVE + ITEMS + IN,
                HOLDER + NOT_HAVE + AMOUNT + IN,
                CONTAIN + ITEMS,
                CONTAIN + AMOUNT,
                NOT_CONTAIN + ITEMS,
                NOT_CONTAIN + AMOUNT);
    }

    private static Optional<Condition> create(Match match, ParseScope scope) {
        int index = match.patternIndex();
        if (index >= FIRST_CONTAINS && !Inventories.isInventory(match.slot(0))) {
            return Optional.empty();
        }
        boolean counted = index % 2 == 1;
        Expression amount = counted ? match.slot(1) : null;
        Expression items = counted ? match.slot(2) : match.slot(1);
        if (counted && !(amount instanceof ConstantExpression)) {
            return Optional.empty();
        }
        return Optional.of(new CondHasItem(amount, items, NEGATED.contains(index)));
    }

    @Override
    public boolean test(Context context) {
        int needed = amount == null ? 1 : (int) Math.ceil((Double) amount.evaluate(context));
        Object value = items.evaluate(context);
        List<?> wanted = value instanceof List<?> list ? list : List.of(value);
        boolean any = items instanceof ListExpression list && list.disjunctive();
        boolean result = any
                ? wanted.stream().anyMatch(item -> enough(item, needed, context))
                : !wanted.isEmpty() && wanted.stream().allMatch(item -> enough(item, needed, context));
        return negate != result;
    }

    private static boolean enough(Object item, int needed, Context context) {
        return item instanceof BlockType type && Inventories.count(context.world(), type) >= needed;
    }
}
