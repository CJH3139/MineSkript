package com.mineskript.client.inventory.elements;

import com.mineskript.common.elements.expressions.ExprEventValue;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Clicked Slot")
@Description({
        "What was clicked in an inventory click event, in Skript's words. clicked slot (or index of clicked slot) is the slot's number in its inventory, as slot %number% of an inventory uses it, and -1 for a click outside the window. clicked inventory is your inventory or the current inventory, whichever holds the slot. clicked item is what was in the slot before the click. click type (or click action) is how it was clicked: left mouse button, right mouse button, left mouse button with shift, right mouse button with shift, middle mouse button, number key, swap offhand, drop key, drop key with control or double click.",
        "They are the same values as event-slot, event-inventory, event-item and event-click type, and only work in on inventory click."
})
@Examples({
        "on inventory click:",
        "	if clicked inventory is the current inventory:",
        "		if clicked slot is 13:",
        "			cancel event",
        "			send \"slot 13 is locked\"",
        "",
        "on inventory click:",
        "	if click type is \"left mouse button with shift\":",
        "		send \"shift-clicked %clicked item%\""
})
@Since("1.0.0-alpha.17")
public final class ExprClicked {
    private ExprClicked() {
    }

    public static void register(SyntaxRegistry registry) {
        add(registry, "slot", SkType.NUMBER, "[the] clicked slot", "[the] [raw] index of [the] clicked slot");
        add(registry, "item", SkType.ITEM, "[the] clicked item");
        add(registry, "inventory", SkType.INVENTORY, "[the] clicked inventory");
        add(registry, "click type", SkType.TEXT, "[the] click (type|action)");
    }

    private static void add(SyntaxRegistry registry, String value, SkType type, String... patterns) {
        registry.addExpression(type, Tier.SIMPLE, (match, scope) -> {
            Event event = scope.event();
            if (event == null || event instanceof Event.EffectCommand || !event.context().provides("click type")) {
                throw new SyntaxException(patterns[0].replace("[the] ", "") + " only works in on inventory click");
            }
            return Optional.of(ExprEventValue.reading(value, type));
        }, patterns);
    }
}
