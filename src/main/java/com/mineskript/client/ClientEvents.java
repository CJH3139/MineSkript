package com.mineskript.client;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.EventFilter;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.AmbiguousExpression;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.EventInfo;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static EventInfo state(SyntaxRegistry registry, String displayName, String pattern, String name) {
        return registry.addEvent(displayName, (match, scope) -> Optional.of(new Event.State(name)), pattern);
    }

    public static EventInfo states(SyntaxRegistry registry, String displayName, String name, String... patterns) {
        return registry.addEvent(displayName, (match, scope) -> Optional.of(new Event.State(name)), patterns);
    }

    public static EventInfo filtered(SyntaxRegistry registry, String displayName, String name, String value,
            String... patterns) {
        return registry.addEvent(displayName,
                (match, scope) -> Optional.of(new Event.State(name, filter(match, value))), patterns);
    }

    private static EventFilter filter(Match match, String value) {
        Optional<Expression> slot = match.slots().stream().filter(Objects::nonNull).findFirst();
        if (slot.isEmpty()) {
            return EventFilter.ANY;
        }
        return new EventFilter(value, fixedValues(slot.get()));
    }

    private static List<Object> fixedValues(Expression expression) {
        Expression unwrapped = fixed(expression);
        if (unwrapped instanceof ConstantExpression constant) {
            return List.of(constant.value());
        }
        if (unwrapped instanceof ListExpression list) {
            List<Object> values = new ArrayList<>();
            for (Expression item : list.items()) {
                if (!(fixed(item) instanceof ConstantExpression constant)) {
                    throw notFixed();
                }
                values.add(constant.value());
            }
            return values;
        }
        throw notFixed();
    }

    private static Expression fixed(Expression expression) {
        if (expression instanceof ConstantExpression) {
            return expression;
        }
        return AmbiguousExpression.alternative(expression).orElse(expression);
    }

    private static SyntaxException notFixed() {
        return new SyntaxException("an event filter must be written as fixed values, such as on break of stone or "
                + "on death of zombie, not a variable or expression");
    }
}
