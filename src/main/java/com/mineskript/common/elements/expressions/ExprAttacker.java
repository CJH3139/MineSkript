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

@Name("Attacker")
@Description({
        "The entity that hurt you in on damage, like Skript's attacker: the mob or player that hit you, or the one that shot the arrow. The same value as event-attacker.",
        "It is not set for damage no entity caused, such as falling or lava, when the server reported no hit, or when the attacker is out of view. It is only available in on damage; using it anywhere else is a parse error."
})
@Examples({
        "on damage:",
        "	if attacker is set:",
        "		send \"hit by %attacker%\""
})
@Since("1.0.0-alpha.18")
public final class ExprAttacker implements Expression {
    private ExprAttacker() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ENTITY, Tier.SIMPLE, (match, scope) -> {
            Event event = scope.event();
            if (event == null || !event.context().provides("attacker")) {
                return Optional.empty();
            }
            return Optional.of(new ExprAttacker());
        }, "[the] attacker");
    }

    @Override
    public SkType type() {
        return SkType.ENTITY;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValue("attacker");
    }
}
