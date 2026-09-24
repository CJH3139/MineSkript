package com.mineskript.script;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

public final class ScriptService {
    private final Path dir;
    private final ScriptLoader loader;
    private final ScriptRegistry registry;
    private final EventDispatcher dispatcher;
    private final VariablePersistence persistence;
    private final ConfigFile config;
    private final LongSupplier nanos;
    private final Map<String, List<ParseError>> errorsByFile = new LinkedHashMap<>();
    private final ScriptSources sources;
    private LoadReport lastReport = new LoadReport(List.of());
    private long lastMillis;
    private boolean reloading;

    public ScriptService(Path dir, ScriptLoader loader, ScriptRegistry registry, EventDispatcher dispatcher, VariablePersistence persistence, ConfigFile config) {
        this(dir, loader, registry, dispatcher, persistence, config, System::nanoTime);
    }

    public ScriptService(Path dir, ScriptLoader loader, ScriptRegistry registry, EventDispatcher dispatcher, VariablePersistence persistence, ConfigFile config, LongSupplier nanos) {
        this.dir = dir;
        this.loader = loader;
        this.registry = registry;
        this.dispatcher = dispatcher;
        this.persistence = persistence;
        this.config = config;
        this.nanos = nanos;
        this.sources = new ScriptSources(dir);
    }

    public LoadReport start() {
        ScriptLoader.createFolder(dir);
        config.create();
        config.load();
        persistence.load();
        return reload();
    }

    public LoadReport reload() {
        if (reloading) {
            return null;
        }
        reloading = true;
        long started = nanos.getAsLong();
        try {
            return loadEverything();
        } finally {
            lastMillis = millis(started);
            reloading = false;
        }
    }

    public LoadReport reloadAll() {
        if (reloading) {
            return null;
        }
        reloading = true;
        long started = nanos.getAsLong();
        try {
            config.create();
            config.load();
            persistence.load();
            return loadEverything();
        } finally {
            lastMillis = millis(started);
            reloading = false;
        }
    }

    public VariablesReload reloadVariables() {
        if (reloading) {
            return VariablesReload.BUSY;
        }
        reloading = true;
        long started = nanos.getAsLong();
        try {
            boolean read = persistence.load();
            persistence.flushWarning();
            return read ? VariablesReload.RELOADED : VariablesReload.UNREADABLE;
        } finally {
            lastMillis = millis(started);
            reloading = false;
        }
    }

    public ConfigReload reloadConfig() {
        if (reloading) {
            return ConfigReload.BUSY;
        }
        reloading = true;
        long started = nanos.getAsLong();
        try {
            config.create();
            return config.load() ? ConfigReload.RELOADED : ConfigReload.UNREADABLE;
        } finally {
            lastMillis = millis(started);
            reloading = false;
        }
    }

    public ConfigFile config() {
        return config;
    }

    public FileReload reloadFile(String file) {
        if (reloading) {
            return new FileReload(file, FileReload.Outcome.BUSY, List.of(), 0, 0);
        }
        reloading = true;
        long started = nanos.getAsLong();
        try {
            if (!ScriptLoader.isScriptName(file)) {
                return new FileReload(file, FileReload.Outcome.REFUSED, List.of(), 0, millis(started));
            }
            String name = canonical(file);
            ParsedScript running = registry.script(name);
            ScriptNames.Listing folder = ScriptNames.listing(dir);
            if (running == null && folder.readable() && !folder.names().contains(name)) {
                return new FileReload(name, FileReload.Outcome.MISSING, List.of(), 0, millis(started));
            }
            String previous = running == null ? name : running.file();
            if (!previous.equals(name)) {
                errorsByFile.remove(previous);
            }
            Optional<ParsedScript> parsed = loader.loadOne(dir, name, sources);
            if (parsed.isEmpty()) {
                if (running == null) {
                    return new FileReload(name, FileReload.Outcome.MISSING, List.of(), 0, millis(started));
                }
                dropFrames(previous, name);
                registry.removeScript(name);
                errorsByFile.remove(name);
                return new FileReload(name, FileReload.Outcome.REMOVED, List.of(), 0, millis(started));
            }
            ParsedScript script = parsed.get();
            if (!script.errors().isEmpty() && running != null) {
                errorsByFile.put(name, script.errors());
                return new FileReload(name, FileReload.Outcome.KEPT, script.errors(), running.triggers().size(), millis(started));
            }
            dropFrames(previous, name);
            registry.replaceScript(script);
            errorsByFile.put(name, script.errors());
            dispatcher.onLoad(name);
            FileReload.Outcome outcome = running == null ? FileReload.Outcome.ADDED : FileReload.Outcome.RELOADED;
            return new FileReload(name, outcome, script.errors(), script.triggers().size(), millis(started));
        } finally {
            lastMillis = millis(started);
            reloading = false;
        }
    }

    public String canonical(String file) {
        if (!ScriptLoader.isScriptName(file)) {
            return file;
        }
        Optional<String> found = ScriptNames.onDisk(dir, file);
        if (found.isPresent()) {
            return found.get();
        }
        for (String loaded : loadedNames()) {
            if (loaded.equalsIgnoreCase(file)) {
                return loaded;
            }
        }
        return file;
    }

    public List<String> loadedNames() {
        List<String> names = new ArrayList<>();
        for (ParsedScript script : registry.scripts()) {
            names.add(script.file());
        }
        return List.copyOf(names);
    }

    public ScriptSources sources() {
        return sources;
    }

    public List<ParseError> errors() {
        List<ParseError> all = new ArrayList<>();
        for (List<ParseError> errors : errorsByFile.values()) {
            all.addAll(errors);
        }
        all.sort((a, b) -> a.file().equals(b.file()) ? Integer.compare(a.line(), b.line()) : a.file().compareTo(b.file()));
        return List.copyOf(all);
    }

    public LoadReport lastReport() {
        return lastReport;
    }

    public long lastMillis() {
        return lastMillis;
    }

    public boolean saveVariables() {
        return persistence.save();
    }

    public ScriptRegistry registry() {
        return registry;
    }

    public EventDispatcher dispatcher() {
        return dispatcher;
    }

    public VariablePersistence persistence() {
        return persistence;
    }

    public Path dir() {
        return dir;
    }

    private void dropFrames(String previous, String name) {
        dispatcher.unloadScript(name);
        if (!previous.equals(name)) {
            dispatcher.unloadScript(previous);
        }
    }

    private LoadReport loadEverything() {
        dispatcher.reset();
        LoadReport report = loader.load(dir, false, sources);
        lastReport = report;
        errorsByFile.clear();
        for (ParsedScript script : report.scripts()) {
            errorsByFile.put(script.file(), script.errors());
        }
        registry.replace(report.scripts());
        dispatcher.onLoad();
        persistence.flushWarning();
        return report;
    }

    private long millis(long startedNanos) {
        return (nanos.getAsLong() - startedNanos) / 1_000_000L;
    }
}
