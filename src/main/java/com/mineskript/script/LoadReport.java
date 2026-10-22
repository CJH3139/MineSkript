package com.mineskript.script;

import com.mineskript.lang.Language;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import java.util.List;

public record LoadReport(List<ParsedScript> scripts, List<String> oldScripts) {
    private static final int OLD_SCRIPTS_SHOWN = 5;

    public LoadReport(List<ParsedScript> scripts) {
        this(scripts, List.of());
    }

    public LoadReport {
        scripts = List.copyOf(scripts);
        oldScripts = List.copyOf(oldScripts);
    }

    public int scriptCount() {
        return scripts.size();
    }

    public int triggerCount() {
        return scripts.stream().mapToInt(script -> script.triggers().size()).sum();
    }

    public List<ParseError> errors() {
        return scripts.stream().flatMap(script -> script.errors().stream()).toList();
    }

    public String summary() {
        int errors = errors().size();
        return Language.format("loader.summary",
                plural(scriptCount(), "script"), plural(triggerCount(), "trigger"), plural(errors, "error"));
    }

    public String oldScriptsWarning() {
        if (oldScripts.isEmpty()) {
            return null;
        }
        String names = String.join(", ", oldScripts.subList(0, Math.min(OLD_SCRIPTS_SHOWN, oldScripts.size())));
        if (oldScripts.size() > OLD_SCRIPTS_SHOWN) {
            names = Language.format("loader.old-scripts-more", names, oldScripts.size() - OLD_SCRIPTS_SHOWN);
        }
        return Language.format("loader.old-scripts", plural(oldScripts.size(), "script"), names);
    }

    public static String plural(int count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }
}
