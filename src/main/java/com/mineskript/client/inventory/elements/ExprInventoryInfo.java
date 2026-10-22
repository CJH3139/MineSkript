package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.InventoryRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Inventory Rows And Slots")
@Description("How many slots or rows an inventory has, like Skript's amount of rows and amount of slots. Your own inventory has 41 slots (hotbar, storage, armour and offhand) and 4 rows. For the current inventory it is the open screen's slots, and the rows are the slots divided by 9, rounded down: 3 for a chest, 6 for a double chest, 0 for a furnace.")
@Examples({
        "on inventory open:",
        "	if rows of current inventory is 6:",
        "		send \"a double chest\"",
        "",
        "on inventory open:",
        "	send \"%amount of slots of current inventory% slots\""
})
@Since("1.0.0-alpha.17")
public final class ExprInventoryInfo implements Expression {
    private static final int ROW = 9;

    private final Expression inventory;
    private final boolean rows;

    private ExprInventoryInfo(Expression inventory, boolean rows) {
        this.inventory = inventory;
        this.rows = rows;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, Priority.before(Priority.before(Priority.SIMPLE)),
                (match, scope) -> Optional.of(new ExprInventoryInfo(match.slot(0), match.has("rows"))),
                "[the] [(amount|number) of] (rows:rows|slots) of %inventory%",
                "%inventory%'s [(amount|number) of] (rows:rows|slots)");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        InventoryRef which = Inventories.inventory(inventory, context);
        int slots = which == InventoryRef.LOCAL ? Inventories.OWN_SLOTS : context.world().openInventoryItems().size();
        return (double) (rows ? slots / ROW : slots);
    }
}
