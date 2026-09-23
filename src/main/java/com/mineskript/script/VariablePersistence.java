package com.mineskript.script;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.runtime.Variables;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VariablePersistence {
    private final VariableStore store;
    private final Path file;
    private final Variables variables;
    private final GameBridge game;
    private int savedVersion;
    private String pendingWarning;

    public VariablePersistence(VariableStore store, Path file, Variables variables, GameBridge game) {
        this.store = store;
        this.file = file;
        this.variables = variables;
        this.game = game;
    }

    public Path file() {
        return file;
    }

    public boolean load() {
        VariableStore.Loaded loaded = store.load(file);
        variables.replaceGlobals(loaded.values());
        savedVersion = variables.version();
        if (loaded.warning() != null) {
            pendingWarning = loaded.warning();
            return false;
        }
        return true;
    }

    public void flushWarning() {
        if (pendingWarning != null && game.hasWorld()) {
            game.showError(pendingWarning);
            pendingWarning = null;
        }
    }

    public boolean dirty() {
        return variables.version() != savedVersion;
    }

    public boolean save() {
        if (!dirty()) {
            return false;
        }
        int version = variables.version();
        try {
            store.save(file, normalised());
            savedVersion = version;
            return true;
        } catch (IOException error) {
            game.showError("could not save " + file.getFileName() + ": " + error.getMessage());
            return false;
        }
    }

    private Map<String, Object> normalised() {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : variables.global().entrySet()) {
            Object value = normalise(entry.getValue());
            if (value != None.NONE) {
                values.put(entry.getKey(), value);
            }
        }
        return values;
    }

    private Object normalise(Object value) {
        if (value instanceof BlockValue block) {
            return block.type();
        }
        if (value instanceof PlayerRef) {
            return game.hasWorld() ? game.playerName() : "player";
        }
        if (value instanceof List<?> list) {
            List<Object> items = new ArrayList<>();
            for (Object item : list) {
                Object normalised = normalise(item);
                if (normalised != None.NONE) {
                    items.add(normalised);
                }
            }
            return List.copyOf(items);
        }
        return value;
    }
}
