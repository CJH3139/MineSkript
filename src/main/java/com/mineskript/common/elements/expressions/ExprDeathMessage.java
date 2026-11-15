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

@Name("Death Message")
@Description({
        "The message the server sent for your death in on death, like Skript's death message, such as Alex fell from a high place, with formatting removed. The same value as event-death message.",
        "It is not set when the server sent none, and it cannot be changed, since the server already sent it. It is only available in on death; using it anywhere else is a parse error."
})
@Examples({
        "on death:",
        "	if death message contains \"lava\":",
        "		send \"stay away from lava\""
})
@Since("1.0.0-alpha.18")
public final class ExprDeathMessage implements Expression {
    private ExprDeathMessage() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> {
            Event event = scope.event();
            if (event == null || !event.context().provides("death message")) {
                return Optional.empty();
            }
            return Optional.of(new ExprDeathMessage());
        }, "[the] death message");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValue("death message");
    }
}
