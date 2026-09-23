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
    private int generation;

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
        generation++;
    }

    public void replaceScript(ParsedScript script) {
        List<ParsedScript> next = new ArrayList<>(scripts);
        int at = indexOf(script.file());
        if (at < 0) {
            next.add(script);
            next.sort((a, b) -> a.file().compareTo(b.file()));
        } else {
            next.set(at, script);
        }
        replace(next);
    }

    public void removeScript(String file) {
        int at = indexOf(file);
        List<ParsedScript> next = new ArrayList<>();
        for (int i = 0; i < scripts.size(); i++) {
            if (i != at) {
                next.add(scripts.get(i));
            }
        }
        replace(next);
    }

    public ParsedScript script(String file) {
        int at = indexOf(file);
        return at < 0 ? null : scripts.get(at);
    }

    private int indexOf(String file) {
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i).file().equals(file)) {
                return i;
            }
        }
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i).file().equalsIgnoreCase(file)) {
                return i;
            }
        }
        return -1;
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

    public int generation() {
        return generation;
    }
}
