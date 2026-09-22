package com.mineskript.script;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import java.util.List;

public record LoadReport(List<ParsedScript> scripts) {
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
        return "Loaded " + plural(scriptCount(), "script") + ", " + plural(triggerCount(), "trigger") + ", " + plural(errors, "error");
    }

    public static String plural(int count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }
}
