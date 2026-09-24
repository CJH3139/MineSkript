package com.mineskript.lang.ast;

import java.util.List;
import java.util.Optional;

/**
 * What an event gives the triggers that handle it: the values they can read and whether they can cancel it. It is
 * declared on the event's registration, so the parser can reject a value or a cancel the event does not support.
 */
public record EventContext(List<EventValue> values, boolean cancellable) {
    /** An event with no values that cannot be cancelled. */
    public static final EventContext NONE = new EventContext(List.of(), false);

    public EventContext {
        values = List.copyOf(values);
    }

    public Optional<EventValue> value(String name) {
        return values.stream().filter(value -> value.name().equals(name)).findFirst();
    }

    public boolean provides(String name) {
        return value(name).isPresent();
    }

    /** The names of the values, in declaration order. */
    public List<String> names() {
        return values.stream().map(EventValue::name).toList();
    }

    // Kept short because the expression parser uses the event's text as part of its cache key.
    @Override
    public String toString() {
        return names() + (cancellable ? " cancellable" : "");
    }
}
