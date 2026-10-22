package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Cursor Slot")
@Description("The item stack you are carrying on the mouse cursor in an inventory screen, like Skript's cursor slot of player. Air when the cursor is empty or no inventory is open. It can be read but not set; click a slot to put it down.")
@Examples({
        "on inventory click:",
        "	wait 1 tick",
        "	send \"now carrying %cursor slot of player%\"",
        "",
        "on key press of \"u\":",
        "	if the cursor item is not air:",
        "		send \"you are carrying %cursor item%\""
})
@Since("1.0.0-alpha.17")
public final class ExprCursorItem implements Expression {
    private ExprCursorItem() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ITEM, Tier.PROPERTY, (match, scope) -> Optional.of(new ExprCursorItem()),
                "[the] cursor slot [of %players%]",
                "%players%'s cursor slot",
                "[the] (cursor item|item on [the] cursor)");
    }

    @Override
    public SkType type() {
        return SkType.ITEM;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().cursorItem();
    }
}
