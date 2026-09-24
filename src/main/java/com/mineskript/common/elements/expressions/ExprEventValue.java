package com.mineskript.common.elements.expressions;

import com.mineskript.doc.NoDoc;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@NoDoc
public final class ExprEventValue implements Expression {
    private final String name;
    private final SkType type;

    private ExprEventValue(String name, SkType type) {
        this.name = name;
        this.type = type;
    }

    public static void register(SyntaxRegistry registry) {
        for (EventValue value : registry.eventValues()) {
            if (value.generic()) {
                registerValue(registry, value);
            }
        }
    }

    public static void registerValue(SyntaxRegistry registry, EventValue value) {
        String name = value.name();
        registry.addExpression(value.type(), Tier.SIMPLE, (match, scope) -> {
            Event event = scope.event();
            if (event instanceof Event.EffectCommand) {
                throw new SyntaxException("event-" + name + " needs an event, and an effect command typed in chat has none");
            }
            if (event == null || !event.context().provides(name)) {
                throw new SyntaxException("event-" + name + " is not available in this event");
            }
            return Optional.of(new ExprEventValue(name, value.type()));
        }, value.syntax());
    }

    public static Expression reading(String name, SkType type) {
        return new ExprEventValue(name, type);
    }

    @Override
    public SkType type() {
        return type;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValue(name);
    }
}
