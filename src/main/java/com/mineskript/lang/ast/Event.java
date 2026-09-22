package com.mineskript.lang.ast;

public sealed interface Event {
    record Periodic(int intervalTicks) implements Event {
    }

    record Load() implements Event {
    }

    record Chat() implements Event {
    }

    record KeyPress(String keyId) implements Event {
    }

    record KeyRelease(String keyId) implements Event {
    }
}
