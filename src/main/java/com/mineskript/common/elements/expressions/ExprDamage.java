package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Damage")
@Description({
        "How much health you lost in on damage, like Skript's damage: the same number as event-damage, in health points (1 point is half a heart, so 20 is a full bar without bonuses). Always positive.",
        "It is only available in on damage; using it anywhere else is a parse error. MineSkript sees the hit after the game has applied it, so the damage cannot be changed."
})
@Examples({
        "on damage:",
        "\tif damage is greater than 4:",
        "\t\tsend title \"ouch\" with subtitle \"lost %damage% health\""
})
@Since("1.0.0-alpha.11")
public final class ExprDamage implements Expression {
    private ExprDamage() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> {
            Event event = scope.event();
            if (event == null || !event.context().provides("damage")) {
                return Optional.empty();
            }
            return Optional.of(new ExprDamage());
        }, "[the] damage");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValue("damage");
    }
}
