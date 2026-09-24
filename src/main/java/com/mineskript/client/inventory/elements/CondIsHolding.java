package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Is Holding")
@Description("Checks whether the item in your main hand is of the given type. Only the selected hotbar slot counts; the offhand is not checked.")
@Examples({
        "on key press of \"r\":",
        "	if player is holding diamond pickaxe:",
        "		hold attack",
        "		wait 3 seconds",
        "		release attack",
        "",
        "on key press of \"b\":",
        "	if player isn't holding bow:",
        "		send \"pick up a bow first\"",
        "		stop",
        "	hold use",
        "	wait 1 second",
        "	release use"
})
@Since("1.0.0-alpha.2")
public final class CondIsHolding implements Condition {
    private final Expression item;
    private final boolean negate;

    private CondIsHolding(Expression item, boolean negate) {
        this.item = item;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "%player% (is|are) holding %itemtype%",
                "%player% (isn't|is not|aren't|are not) holding %itemtype%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression item = match.slot(1);
        if (item.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondIsHolding(item, negate));
    }

    @Override
    public boolean test(Context context) {
        Object value = item.evaluate(context);
        boolean result = false;
        if (value instanceof BlockType type) {
            result = context.world().heldItem().id().equals(type.id());
        }
        return negate != result;
    }
}
