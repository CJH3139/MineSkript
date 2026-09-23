package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ExprEventValue implements Expression {
    private final String name;
    private final SkType type;

    private ExprEventValue(String name, SkType type) {
        this.name = name;
        this.type = type;
    }

    public static void register(SyntaxRegistry registry) {
        add(registry, "damage", SkType.NUMBER, Set.of("damage"));
        add(registry, "healed", SkType.NUMBER, Set.of("heal"));
        add(registry, "health change", SkType.NUMBER, Set.of("health"));
        add(registry, "old health", SkType.NUMBER, Set.of("health"));
        add(registry, "durability", SkType.NUMBER, Set.of("durability"));
        add(registry, "block", SkType.BLOCKTYPE, Set.of("block break", "block place"));
        add(registry, "screen title", SkType.TEXT, Set.of("screen open"));
        add(registry, "screen type", SkType.TEXT, Set.of("screen open"));
        add(registry, "hunger change", SkType.NUMBER, Set.of("hunger"));
        add(registry, "level change", SkType.NUMBER, Set.of("level"));
        add(registry, "xp change", SkType.NUMBER, Set.of("xp"));
        add(registry, "fall distance", SkType.NUMBER, Set.of("land"));
        add(registry, "item", SkType.ITEM, Set.of("held", "inventory", "use start", "use stop", "consume", "item break", "durability"));
        add(registry, "previous item", SkType.ITEM, Set.of("held"));
        add(registry, "gamemode", SkType.TEXT, Set.of("gamemode"));
        add(registry, "effect", SkType.TEXT, Set.of("effect gain", "effect lose"));
        add(registry, "effect level", SkType.NUMBER, Set.of("effect gain"));
        add(registry, "entity", SkType.ENTITY, Set.of("mount", "dismount"));
        add(registry, "from dimension", SkType.TEXT, Set.of("dimension"));
        add(registry, "to dimension", SkType.TEXT, Set.of("dimension"));
        add(registry, "player", SkType.TEXT, Set.of("player join", "player leave"));
        for (String axis : List.of("x", "y", "z")) {
            add(registry, "block " + axis, SkType.NUMBER, Set.of("block break", "block place"));
            add(registry, "from " + axis, SkType.NUMBER, Set.of("move"));
            add(registry, "to " + axis, SkType.NUMBER, Set.of("move"));
        }
    }

    private static void add(SyntaxRegistry registry, String name, SkType type, Set<String> events) {
        registry.addExpression(type, Tier.SIMPLE, (match, scope) -> {
            if (scope.event() instanceof Event.EffectCommand) {
                throw new SyntaxException("event-" + name + " needs an event, and an effect command typed in chat has none");
            }
            String event = scope.event() instanceof Event.State state ? state.name()
                    : scope.event() instanceof Event.Durability ? "durability" : null;
            if (event == null || !events.contains(event)) {
                throw new SyntaxException("event-" + name + " is not available in this event");
            }
            return Optional.of(new ExprEventValue(name, type));
        }, "event-" + name);
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
