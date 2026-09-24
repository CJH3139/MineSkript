package com.mineskript.lang.ast;

/**
 * A value an event hands to its triggers, such as the damage of on damage. It is defined once, with one
 * description, and events declare which values they provide by name.
 *
 * @param name the key the value is stored under while a trigger runs, such as {@code damage}
 * @param type the type scripts read it as
 * @param syntax how scripts write it, such as {@code event-damage} or {@code message}
 * @param description what the value holds, for the documentation
 */
public record EventValue(String name, SkType type, String syntax, String description) {
    /** A value read with the generic {@code event-<name>} expression. */
    public static EventValue of(String name, SkType type, String description) {
        return new EventValue(name, type, "event-" + name, description);
    }

    /** Whether scripts read this value as {@code event-<name>}, rather than through an expression of its own. */
    public boolean generic() {
        return syntax.equals("event-" + name);
    }
}
