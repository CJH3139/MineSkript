package com.mineskript.lang.runtime;

import com.mineskript.game.GameBridge;
import java.util.Map;

public final class Context {
    private final GameBridge game;
    private final String file;
    private final Map<String, Object> eventValues;

    public Context(GameBridge game, String file, Map<String, Object> eventValues) {
        this.game = game;
        this.file = file;
        this.eventValues = Map.copyOf(eventValues);
    }

    public GameBridge game() {
        return game;
    }

    public String file() {
        return file;
    }

    public Object eventValue(String key) {
        Object value = eventValues.get(key);
        if (value == null) {
            throw new ScriptError("\"" + key + "\" is not available in this event");
        }
        return value;
    }

    public GameBridge world() {
        if (!game.hasWorld()) {
            throw new ScriptError("no world");
        }
        return game;
    }
}
