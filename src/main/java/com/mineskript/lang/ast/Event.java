package com.mineskript.lang.ast;

public sealed interface Event {
    record Periodic(int intervalTicks) implements Event {
    }

    record Load() implements Event {
    }

    record Chat() implements Event {
    }

    record ChatSend() implements Event {
    }

    record CommandSend() implements Event {
    }

    record KeyPress(String keyId, java.util.List<String> modifiers) implements Event {
        public KeyPress(String keyId) {
            this(keyId, java.util.List.of());
        }
    }

    record KeyRelease(String keyId) implements Event {
    }

    record State(String name) implements Event {
    }

    record Durability(double threshold) implements Event {
    }

    record EffectCommand() implements Event {
    }
}
