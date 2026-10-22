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
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Drop From Slot")
@Description("Throws items out of an inventory slot, like pressing Q over it in an inventory screen: drop slot 5 throws one item, and drop the stack in slot 5 (or drop the whole stack from slot 5) throws all of it, like Ctrl+Q. Slots are numbered like slot %number% of an inventory, and without of it is your own inventory, which does not need to be open. To drop what you are holding in your hand, use drop item.")
@Examples({
        "on inventory change:",
        "	loop 36 times:",
        "		set {_slot} to loop-iteration - 1",
        "		if slot {_slot} of player's inventory is rotten flesh:",
        "			drop the stack in slot {_slot}",
        "",
        "on key press of \"g\":",
        "	drop slot 0 of current inventory"
})
@Since("1.0.0-alpha.17")
public final class EffDropSlot implements Statement {
    private static final String SLOT = " slot %number% [(of|in) %inventory%]";

    private final int line;
    private final Expression slot;
    private final Expression inventory;
    private final boolean wholeStack;

    private EffDropSlot(int line, Expression slot, Expression inventory, boolean wholeStack) {
        this.line = line;
        this.slot = slot;
        this.inventory = inventory;
        this.wholeStack = wholeStack;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffDropSlot::create,
                "drop [(one|an) item (from|in|of)]" + SLOT,
                "drop [the] (stack|whole stack|all) (in|from|of)" + SLOT);
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffDropSlot(scope.line(), match.slot(0), match.slot(1), match.patternIndex() == 1));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().clickSlot(Inventories.inventory(inventory, context), Inventories.slot(slot, context),
                new InventoryClick(InventoryClick.Kind.THROW, wholeStack ? 1 : 0));
        return Flow.CONTINUE;
    }
}
