package com.mineskript.lang.parse;

import com.mineskript.lang.ast.EventContext;
import com.mineskript.lang.ast.EventValue;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One registered event: its documentation and its context (the values it provides and whether it can be
 * cancelled), filled in with the chained calls after {@link SyntaxRegistry#addEvent(String, SyntaxFactory, String...)},
 * the same way Skript documents its events.
 */
public final class EventInfo {
    private final String name;
    private final List<String> patterns;
    private String[] description = new String[0];
    private String[] examples = new String[0];
    private String[] since = new String[0];
    private String[] keywords = new String[0];
    private final Map<String, EventValue> knownValues;
    private final Set<String> values = new LinkedHashSet<>();
    private boolean cancellable;

    EventInfo(String name, List<String> patterns, Map<String, EventValue> knownValues) {
        this.name = name;
        this.patterns = List.copyOf(patterns);
        this.knownValues = knownValues;
    }

    /**
     * Declares the values this event provides, by the names they were defined with in
     * {@link SyntaxRegistry#addEventValue(EventValue)}. Triggers of this event may read exactly these.
     */
    public EventInfo values(String... names) {
        for (String value : names) {
            if (!knownValues.containsKey(value)) {
                throw new IllegalArgumentException("event " + this.name + " declares the undefined event value " + value);
            }
            values.add(value);
        }
        return this;
    }

    /** Declares that triggers of this event may cancel it. */
    public EventInfo cancellable() {
        cancellable = true;
        return this;
    }

    /** The values and cancellability this event declared, with the values in the order they were defined. */
    public EventContext context() {
        List<EventValue> declared = knownValues.values().stream().filter(value -> values.contains(value.name())).toList();
        return new EventContext(declared, cancellable);
    }

    public EventInfo description(String... description) {
        this.description = description.clone();
        return this;
    }

    public EventInfo examples(String... examples) {
        this.examples = examples.clone();
        return this;
    }

    public EventInfo since(String... since) {
        this.since = since.clone();
        return this;
    }

    public EventInfo keywords(String... keywords) {
        this.keywords = keywords.clone();
        return this;
    }

    public String name() {
        return name;
    }

    public List<String> patterns() {
        return patterns;
    }

    public List<String> description() {
        return List.of(description);
    }

    public List<String> examples() {
        return List.of(examples);
    }

    public List<String> since() {
        return List.of(since);
    }

    public List<String> keywords() {
        return List.of(keywords);
    }
}
