package com.mineskript.lang.ast;

import java.util.List;
import java.util.Optional;

public record EventContext(List<EventValue> values, boolean cancellable, boolean instant) {
    public static final EventContext NONE = new EventContext(List.of(), false);

    public EventContext {
        values = List.copyOf(values);
    }

    public EventContext(List<EventValue> values, boolean cancellable) {
        this(values, cancellable, false);
    }

    public Optional<EventValue> value(String name) {
        return values.stream().filter(value -> value.name().equals(name)).findFirst();
    }

    public boolean provides(String name) {
        return value(name).isPresent();
    }

    public List<String> names() {
        return values.stream().map(EventValue::name).toList();
    }

    @Override
    public String toString() {
        return names() + (cancellable ? " cancellable" : "") + (instant ? " instant" : "");
    }
}
