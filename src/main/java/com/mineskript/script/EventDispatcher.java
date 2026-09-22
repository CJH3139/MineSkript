package com.mineskript.script;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Execution;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.ScriptError;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventDispatcher {
    private final ScriptRegistry registry;
    private final GameBridge game;
    private final Interpreter interpreter;
    private final Scheduler scheduler;
    private final Map<String, Boolean> keyState = new HashMap<>();
    private long ticks;

    public EventDispatcher(ScriptRegistry registry, GameBridge game, Interpreter interpreter, Scheduler scheduler) {
        this.registry = registry;
        this.game = game;
        this.interpreter = interpreter;
        this.scheduler = scheduler;
    }

    public long ticks() {
        return ticks;
    }

    public void tick() {
        if (!game.hasWorld()) {
            return;
        }
        ticks++;
        for (Execution execution : scheduler.drain(ticks)) {
            runSafely(execution);
        }
        for (Trigger trigger : registry.triggers()) {
            if (trigger.event() instanceof Event.Periodic periodic && ticks % periodic.intervalTicks() == 0) {
                start(trigger, Map.of());
            }
        }
        pollKeys();
    }

    public void onChat(String message) {
        if (!game.hasWorld()) {
            return;
        }
        for (Trigger trigger : registry.triggers()) {
            if (trigger.event() instanceof Event.Chat) {
                start(trigger, Map.of("message", message));
            }
        }
    }

    public void onLoad() {
        for (Trigger trigger : registry.triggers()) {
            if (trigger.event() instanceof Event.Load) {
                start(trigger, Map.of());
            }
        }
    }

    public void onDisconnect() {
        scheduler.clear();
        keyState.clear();
        game.releaseAll();
    }

    public void reset() {
        onDisconnect();
        ticks = 0;
    }

    private void pollKeys() {
        for (String keyId : registry.watchedKeys()) {
            boolean down = game.isKeyDown(keyId);
            Boolean previous = keyState.put(keyId, down);
            if (previous == null || previous == down) {
                continue;
            }
            for (Trigger trigger : List.copyOf(registry.triggers())) {
                if (down && trigger.event() instanceof Event.KeyPress press && press.keyId().equals(keyId)) {
                    start(trigger, Map.of());
                } else if (!down && trigger.event() instanceof Event.KeyRelease release && release.keyId().equals(keyId)) {
                    start(trigger, Map.of());
                }
            }
        }
    }

    private void start(Trigger trigger, Map<String, Object> values) {
        runSafely(new Execution(trigger, new Context(game, trigger.file(), values)));
    }

    private void runSafely(Execution execution) {
        try {
            if (interpreter.run(execution) == Interpreter.Outcome.WAITING) {
                scheduler.schedule(execution, ticks + execution.waitTicks());
            }
        } catch (ScriptError error) {
            game.showError(error.toString());
        }
    }
}
