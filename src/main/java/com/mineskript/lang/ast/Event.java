package com.mineskript.lang.ast;

import java.util.List;
import java.util.Map;

public sealed interface Event {
    EventContext context();

    default Event withContext(EventContext context) {
        return switch (this) {
            case Periodic event -> new Periodic(event.intervalTicks(), context);
            case Load event -> new Load(context);
            case Chat event -> new Chat(context);
            case ChatSend event -> new ChatSend(context);
            case CommandSend event -> new CommandSend(context);
            case KeyPress event -> new KeyPress(event.keyId(), event.modifiers(), context);
            case KeyRelease event -> new KeyRelease(event.keyId(), context);
            case State event -> new State(event.name(), context, event.filter());
            case Durability event -> new Durability(event.threshold(), context);
            case EffectCommand event -> new EffectCommand(context);
        };
    }

    record Periodic(int intervalTicks, EventContext context) implements Event {
        public Periodic(int intervalTicks) {
            this(intervalTicks, EventContext.NONE);
        }
    }

    record Load(EventContext context) implements Event {
        public Load() {
            this(EventContext.NONE);
        }
    }

    record Chat(EventContext context) implements Event {
        public Chat() {
            this(EventContext.NONE);
        }
    }

    record ChatSend(EventContext context) implements Event {
        public ChatSend() {
            this(EventContext.NONE);
        }
    }

    record CommandSend(EventContext context) implements Event {
        public CommandSend() {
            this(EventContext.NONE);
        }
    }

    record KeyPress(String keyId, List<String> modifiers, EventContext context) implements Event {
        public KeyPress(String keyId, List<String> modifiers) {
            this(keyId, modifiers, EventContext.NONE);
        }

        public KeyPress(String keyId) {
            this(keyId, List.of());
        }
    }

    record KeyRelease(String keyId, EventContext context) implements Event {
        public KeyRelease(String keyId) {
            this(keyId, EventContext.NONE);
        }
    }

    record State(String name, EventContext context, EventFilter filter) implements Event {
        public State(String name, EventContext context) {
            this(name, context, EventFilter.ANY);
        }

        public State(String name) {
            this(name, EventContext.NONE);
        }

        public State(String name, EventFilter filter) {
            this(name, EventContext.NONE, filter);
        }

        public boolean accepts(Map<String, Object> values) {
            return filter.accepts(values);
        }
    }

    record Durability(double threshold, EventContext context) implements Event {
        public Durability(double threshold) {
            this(threshold, EventContext.NONE);
        }
    }

    record EffectCommand(EventContext context) implements Event {
        public EffectCommand() {
            this(EventContext.NONE);
        }
    }
}
