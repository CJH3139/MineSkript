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

@Name("Damage Cause")
@Description({
        "What hurt you in on damage, like Skript's damage cause: the same value as event-damage cause, with Skript's names such as fall, lava, fire, burning, drowning, entity attack, projectile, entity explosion, magic, wither and void.",
        "It comes from the hit the server reports, so it is not set when the server reported none. It is only available in on damage; using it anywhere else is a parse error."
})
@Examples({
        "on damage:",
        "	if damage cause is fall or lava:",
        "		send \"careful\""
})
@Since("1.0.0-alpha.18")
public final class ExprDamageCause implements Expression {
    private ExprDamageCause() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.DAMAGECAUSE, Tier.SIMPLE, (match, scope) -> {
            Event event = scope.event();
            if (event == null || !event.context().provides("damage cause")) {
                return Optional.empty();
            }
            return Optional.of(new ExprDamageCause());
        }, "[the] damage cause");
    }

    @Override
    public SkType type() {
        return SkType.DAMAGECAUSE;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValue("damage cause");
    }
}
