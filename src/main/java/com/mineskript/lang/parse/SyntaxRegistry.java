package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.function.FunctionBody;
import com.mineskript.lang.function.FunctionInfo;
import com.mineskript.lang.function.FunctionParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SyntaxRegistry {
    /** The name {@link #addon()} gives syntax that ships with MineSkript itself. */
    public static final String BUILT_IN = "MineSkript";

    /**
     * One registered syntax element, the class that registered it, the addon it came from ({@link #BUILT_IN} for
     * MineSkript's own) and the path of the module that registered it (such as {@code client/inventory}, or
     * {@code null} outside any module), kept only by a registry made with {@link #documenting()} so documentation can
     * be generated in registration order. {@code event} is set for events and {@code function} for built-in
     * functions, whose only pattern is their signature.
     */
    public record Registration(String kind, Class<?> owner, List<String> patterns, SkType returnType, EventInfo event,
            FunctionInfo function, String addon, String module) {
    }

    private final List<SyntaxEntry<Event>> events = new ArrayList<>();
    private final List<Registration> registrations = new ArrayList<>();
    private boolean documenting;
    private String addon = BUILT_IN;
    private String module;
    private final List<EventInfo> eventInfos = new ArrayList<>();
    private final Map<String, EventValue> eventValues = new LinkedHashMap<>();
    private final List<SyntaxEntry<Statement>> effects = new ArrayList<>();
    private final List<SyntaxEntry<Condition>> conditions = new ArrayList<>();
    private final Map<Tier, List<ExpressionEntry>> expressions = new EnumMap<>(Tier.class);
    private final Map<String, FunctionInfo> functions = new LinkedHashMap<>();

    public SyntaxRegistry() {
        for (Tier tier : Tier.values()) {
            expressions.put(tier, new ArrayList<>());
        }
    }

    /** A registry that also remembers which class registered each element, for the documentation generator. */
    public static SyntaxRegistry documenting() {
        SyntaxRegistry registry = new SyntaxRegistry();
        registry.documenting = true;
        return registry;
    }

    public List<Registration> registrations() {
        return registrations;
    }

    /** The addon whose syntax is being registered now, or {@link #BUILT_IN}. */
    public String addon() {
        return addon;
    }

    /**
     * Runs a registration on behalf of an addon, so everything it registers is recorded as coming from that addon.
     * The addon goes back to what it was afterwards, even when the registration throws.
     */
    public void registerAs(String addonName, Consumer<SyntaxRegistry> registration) {
        String outer = addon;
        addon = addonName;
        try {
            registration.accept(this);
        } finally {
            addon = outer;
        }
    }

    /** The path of the module whose syntax is being registered now, such as {@code client/inventory}, if any. */
    public Optional<String> module() {
        return Optional.ofNullable(module);
    }

    /**
     * Runs a registration on behalf of a module, so everything it registers is recorded as coming from that module
     * path. The module goes back to what it was afterwards, even when the registration throws.
     */
    public void registerIn(String modulePath, Consumer<SyntaxRegistry> registration) {
        String outer = module;
        module = modulePath;
        try {
            registration.accept(this);
        } finally {
            module = outer;
        }
    }

    private void record(String kind, String[] patterns, SkType returnType, EventInfo event) {
        record(kind, patterns, returnType, event, null);
    }

    private void record(String kind, String[] patterns, SkType returnType, EventInfo event, FunctionInfo function) {
        if (documenting) {
            registrations.add(new Registration(kind, owner(), List.of(patterns), returnType, event, function, addon,
                    module));
        }
    }

    private static Class<?> owner() {
        List<Class<?>> callers = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames.map(StackWalker.StackFrame::getDeclaringClass)
                        .filter(type -> type != SyntaxRegistry.class)
                        .toList());
        for (Class<?> caller : callers) {
            for (java.lang.annotation.Annotation annotation : caller.getAnnotations()) {
                if (annotation.annotationType().getPackageName().equals("com.mineskript.doc")) {
                    return caller;
                }
            }
        }
        return callers.isEmpty() ? null : callers.get(0);
    }

    /**
     * Defines a value that events can provide. Each value is defined once, with one description, and events name it
     * in {@link EventInfo#values(String...)}. Define values before the events that provide them.
     */
    public EventValue addEventValue(EventValue value) {
        if (eventValues.putIfAbsent(value.name(), value) != null) {
            throw new IllegalArgumentException("event value " + value.name() + " is already defined");
        }
        return value;
    }

    /** Every defined event value, in the order they were defined. */
    public List<EventValue> eventValues() {
        return List.copyOf(eventValues.values());
    }

    /**
     * Registers an event. The events the factory creates carry the context declared on the returned
     * {@link EventInfo}, so parse-time checks can see which values the event provides and whether it can be
     * cancelled.
     */
    public EventInfo addEvent(String name, SyntaxFactory<Event> factory, String... patterns) {
        EventInfo info = new EventInfo(name, Arrays.asList(patterns), eventValues);
        SyntaxFactory<Event> declared = (match, scope) -> factory.create(match, scope)
                .map(event -> event.withContext(info.context()));
        events.add(new SyntaxEntry<>(compile(patterns), declared));
        eventInfos.add(info);
        record("event", patterns, null, info);
        return info;
    }

    public List<EventInfo> eventInfos() {
        return eventInfos;
    }

    /**
     * Registers an effect: a line that does something. The factory is called with the match of the first pattern that
     * fits and may return empty to let later patterns and elements try; throwing {@link SyntaxException} makes the
     * line a parse error with that message. It has {@link Priority#SIMPLE} priority.
     */
    public void addEffect(SyntaxFactory<Statement> factory, String... patterns) {
        addEffect(Priority.SIMPLE, factory, patterns);
    }

    /**
     * Registers an effect with a priority: effects are tried by priority, then in registration order, so a
     * catch-all pattern such as {@code set %objects% to %objects%} goes after the specific ones wherever it is
     * registered.
     */
    public void addEffect(Priority priority, SyntaxFactory<Statement> factory, String... patterns) {
        insert(effects, new SyntaxEntry<>(compile(patterns), factory, priority));
        record("effect", patterns, null, null);
    }

    /**
     * Registers a condition, for use after if, while, wait until and the like. Put negated patterns here too. It has
     * {@link Priority#SIMPLE} priority.
     */
    public void addCondition(SyntaxFactory<Condition> factory, String... patterns) {
        addCondition(Priority.SIMPLE, factory, patterns);
    }

    /**
     * Registers a condition with a priority: conditions are tried by priority, then in registration order, so a
     * catch-all pattern such as {@code %objects% is %objects%} goes after the specific ones wherever it is registered.
     */
    public void addCondition(Priority priority, SyntaxFactory<Condition> factory, String... patterns) {
        insert(conditions, new SyntaxEntry<>(compile(patterns), factory, priority));
        record("condition", patterns, null, null);
    }

    private static <T> void insert(List<SyntaxEntry<T>> entries, SyntaxEntry<T> entry) {
        entries.add(insertionPoint(entries, SyntaxEntry::priority, entry.priority()), entry);
    }

    /** Where a new entry goes: after every entry of the same or a lower priority, so registration order breaks ties. */
    private static <E> int insertionPoint(List<E> entries, Function<E, Priority> priorityOf, Priority priority) {
        int index = entries.size();
        while (index > 0 && priorityOf.apply(entries.get(index - 1)).compareTo(priority) > 0) {
            index--;
        }
        return index;
    }

    /**
     * Registers an expression that gives a value of the return type. Simpler tiers are tried first; a pattern must
     * need at least one word or a second value besides its only slot, or registering it throws. It has
     * {@link Priority#SIMPLE} priority within its tier.
     */
    public void addExpression(SkType returnType, Tier tier, SyntaxFactory<Expression> factory, String... patterns) {
        addExpression(returnType, tier, Priority.SIMPLE, factory, patterns);
    }

    /**
     * Registers an expression with a priority within its tier: the expressions of a tier are tried by priority, then
     * in registration order.
     */
    public void addExpression(SkType returnType, Tier tier, Priority priority, SyntaxFactory<Expression> factory,
            String... patterns) {
        List<ExpressionEntry> entries = expressions.get(tier);
        entries.add(insertionPoint(entries, ExpressionEntry::priority, priority),
                new ExpressionEntry(compileExpressionPatterns(patterns), returnType, tier, factory, priority));
        record("expression", patterns, returnType, null);
    }

    /**
     * Registers a built-in function, like Skript's default functions such as {@code round(n, d)}: scripts call it as
     * {@code name(arguments)} anywhere a value can go, or on a line of its own. Document it with the chained calls on
     * the returned {@link FunctionInfo}. The body receives the arguments converted to the parameter types and runs
     * on the game thread, so it must be quick and must not block. Function names ignore case, and a script that
     * defines a function with a built-in function's name gets a parse error.
     *
     * @throws IllegalArgumentException if a function with that name, ignoring case, is already registered, or the
     *     name or parameters are invalid (see {@link FunctionInfo#FunctionInfo})
     */
    public FunctionInfo addFunction(String name, SkType returnType, FunctionBody body,
            FunctionParameter... parameters) {
        FunctionInfo function = new FunctionInfo(name, returnType, body, List.of(parameters));
        if (functions.putIfAbsent(function.key(), function) != null) {
            throw new IllegalArgumentException("function " + name + " is already registered");
        }
        record("function", new String[] {function.name()}, returnType, null, function);
        return function;
    }

    /** The built-in function with this name, ignoring case, if there is one. */
    public Optional<FunctionInfo> function(String name) {
        return Optional.ofNullable(functions.get(name.toLowerCase(Locale.ROOT)));
    }

    /** Every built-in function, in the order they were registered. */
    public List<FunctionInfo> functions() {
        return List.copyOf(functions.values());
    }

    public List<SyntaxEntry<Event>> events() {
        return events;
    }

    public List<SyntaxEntry<Statement>> effects() {
        return effects;
    }

    public List<SyntaxEntry<Condition>> conditions() {
        return conditions;
    }

    public List<ExpressionEntry> expressions(Tier tier) {
        return expressions.get(tier);
    }

    public <T> Optional<T> matchFirst(List<SyntaxEntry<T>> entries, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        for (SyntaxEntry<T> entry : entries) {
            Optional<T> result = matchPatterns(entry.patterns(), entry.factory(), tokens, resolver, scope);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    public Optional<Expression> matchEntry(ExpressionEntry entry, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        return matchPatterns(entry.patterns(), entry.factory(), tokens, resolver, scope);
    }

    private static <T> Optional<T> matchPatterns(List<Pattern> patterns, SyntaxFactory<T> factory, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        for (int i = 0; i < patterns.size(); i++) {
            Optional<Match> match = PatternMatcher.match(patterns.get(i), i, tokens, resolver, scope);
            if (match.isEmpty()) {
                continue;
            }
            Optional<T> result = factory.create(match.get(), scope);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private static List<Pattern> compile(String... patterns) {
        return Arrays.stream(patterns).map(Pattern::compile).toList();
    }

    private static List<Pattern> compileExpressionPatterns(String... patterns) {
        List<Pattern> compiled = compile(patterns);
        for (Pattern pattern : compiled) {
            if (PatternElement.canMatchAsSlotAlone(pattern.root())) {
                throw new IllegalArgumentException("expression pattern can match as a single slot with nothing else required: "
                        + pattern.source()
                        + ". Such a pattern offers its slot the entire token list it is already parsing, so every nested"
                        + " expression re-parses the same tokens and parsing becomes exponential, freezing the client"
                        + " instead of failing. Add a required literal or a second required slot so at least one token is"
                        + " always consumed outside the slot.");
            }
        }
        return compiled;
    }
}
