package com.mineskript.script;

import com.mineskript.game.GameBridge;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ConfigFile {
    private final Path file;
    private final List<String> pending = new ArrayList<>();
    private Config current = Config.DEFAULTS;

    public ConfigFile(Path file) {
        this.file = file;
    }

    public Path file() {
        return file;
    }

    public Config current() {
        return current;
    }

    public String name() {
        Path name = file.getFileName();
        return name == null ? file.toString() : name.toString();
    }

    public boolean create() {
        try {
            if (Files.isRegularFile(file)) {
                return false;
            }
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, Config.TEMPLATE, StandardCharsets.UTF_8);
            return true;
        } catch (IOException error) {
            return false;
        }
    }

    public boolean load() {
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException error) {
            current = Config.DEFAULTS;
            pending.add("could not read " + name() + ", using the defaults: " + error.getMessage());
            return false;
        }
        Config.Loaded loaded = Config.parse(name(), text);
        current = loaded.config();
        pending.addAll(loaded.warnings());
        if (loaded.empty()) {
            pending.add(name() + " has no settings in it, using the defaults");
        }
        return true;
    }

    public List<String> takeWarnings() {
        List<String> warnings = List.copyOf(pending);
        pending.clear();
        return warnings;
    }

    public void flushTo(GameBridge game) {
        if (pending.isEmpty() || !game.hasWorld()) {
            return;
        }
        for (String warning : takeWarnings()) {
            game.showWarning(warning);
        }
    }
}
