package com.mineskript.script;

import java.nio.file.Path;

public final class ScriptService {
    private final Path dir;
    private final ScriptLoader loader;
    private final ScriptRegistry registry;
    private final EventDispatcher dispatcher;

    public ScriptService(Path dir, ScriptLoader loader, ScriptRegistry registry, EventDispatcher dispatcher) {
        this.dir = dir;
        this.loader = loader;
        this.registry = registry;
        this.dispatcher = dispatcher;
    }

    public LoadReport reload() {
        dispatcher.reset();
        LoadReport report = loader.load(dir);
        registry.replace(report.scripts());
        dispatcher.onLoad();
        return report;
    }

    public ScriptRegistry registry() {
        return registry;
    }

    public EventDispatcher dispatcher() {
        return dispatcher;
    }

    public Path dir() {
        return dir;
    }
}
