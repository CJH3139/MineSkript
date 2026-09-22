package com.mineskript.script;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ParsedScript;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ScriptRegistry {
    private List<ParsedScript> scripts = List.of();
    private List<Trigger> triggers = List.of();
    private Set<String> watchedKeys = Set.of();

    public void replace(List<ParsedScript> loaded) {
        List<Trigger> all = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        for (ParsedScript script : loaded) {
            for (Trigger trigger : script.triggers()) {
                all.add(trigger);
                if (trigger.event() instanceof Event.KeyPress press) {
                    keys.add(press.keyId());
                } else if (trigger.event() instanceof Event.KeyRelease release) {
                    keys.add(release.keyId());
                }
            }
        }
        scripts = List.copyOf(loaded);
        triggers = List.copyOf(all);
        watchedKeys = Set.copyOf(keys);
    }

    public List<ParsedScript> scripts() {
        return scripts;
    }

    public List<Trigger> triggers() {
        return triggers;
    }

    public Set<String> watchedKeys() {
        return watchedKeys;
    }
}
