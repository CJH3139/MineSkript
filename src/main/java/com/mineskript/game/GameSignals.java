package com.mineskript.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GameSignals {
    public static final int LIMIT_PER_TICK = 256;

    public record Signal(String event, Map<String, Object> values) {
    }

    private static final List<Signal> queue = new ArrayList<>();
    private static volatile Set<String> listening = Set.of();
    private static volatile Runnable frame = () -> {
    };

    private GameSignals() {
    }

    public static boolean wants(String event) {
        return listening.contains(event);
    }

    public static void listen(Set<String> events) {
        listening = Set.copyOf(events);
    }

    public static void emit(String event, Map<String, Object> values) {
        if (!wants(event)) {
            return;
        }
        synchronized (queue) {
            if (queue.size() < LIMIT_PER_TICK) {
                queue.add(new Signal(event, values));
            }
        }
    }

    public static void emitOnce(String event) {
        if (!wants(event)) {
            return;
        }
        synchronized (queue) {
            for (Signal signal : queue) {
                if (signal.event().equals(event)) {
                    return;
                }
            }
            if (queue.size() < LIMIT_PER_TICK) {
                queue.add(new Signal(event, Map.of()));
            }
        }
    }

    public static List<Signal> drain() {
        synchronized (queue) {
            List<Signal> drained = List.copyOf(queue);
            queue.clear();
            return drained;
        }
    }

    public static void onFrame(Runnable action) {
        frame = action;
    }

    public static void frame() {
        if (wants("frame")) {
            frame.run();
        }
    }
}
