package com.mineskript.doc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.syntax.DefaultSyntax;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentationTest {
    private static final String[] SECTIONS = {"events", "conditions", "effects", "expressions", "functions"};

    @Test
    void everyElementHasANameDescriptionExamplesAndSince() {
        JsonObject docs = JSONGenerator.generate("test");
        List<String> missing = new ArrayList<>();
        for (String section : SECTIONS) {
            for (JsonElement item : docs.getAsJsonArray(section)) {
                JsonObject element = item.getAsJsonObject();
                String id = section + " " + element.get("id").getAsString();
                if (element.get("name").getAsString().isBlank()) {
                    missing.add(id + ": name");
                }
                for (String field : List.of("description", "examples", "since")) {
                    if (element.getAsJsonArray(field).isEmpty()) {
                        missing.add(id + ": " + field);
                    }
                }
            }
        }
        for (JsonElement item : docs.getAsJsonArray("eventValues")) {
            JsonObject value = item.getAsJsonObject();
            if (value.get("description").getAsString().isBlank()) {
                missing.add("event value " + value.get("name").getAsString() + ": description");
            }
        }
        assertEquals(List.of(), missing);
    }

    @Test
    void everyExampleParses() {
        JsonObject docs = JSONGenerator.generate("test");
        List<String> failures = new ArrayList<>();
        int checked = 0;
        for (String section : SECTIONS) {
            for (JsonElement item : docs.getAsJsonArray(section)) {
                JsonObject element = item.getAsJsonObject();
                int n = 0;
                for (String script : scripts(element.getAsJsonArray("examples"))) {
                    checked++;
                    ParsedScript parsed = new Parser(DefaultSyntax.registry()).parse("example.ms", script);
                    String where = element.get("id").getAsString() + " example " + (++n);
                    for (ParseError error : parsed.errors()) {
                        failures.add(where + ": " + error);
                    }
                    if (parsed.errors().isEmpty() && parsed.triggers().isEmpty() && parsed.functions().isEmpty()) {
                        failures.add(where + ": has no trigger or function");
                    }
                }
            }
        }
        System.out.println("checked " + checked + " documentation examples");
        assertEquals(List.of(), failures, String.join("\n", failures));
    }

    private static List<String> scripts(JsonArray lines) {
        List<String> scripts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (JsonElement element : lines) {
            String line = element.getAsString();
            if (line.isEmpty()) {
                if (!current.isEmpty()) {
                    scripts.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(line).append('\n');
        }
        if (!current.isEmpty()) {
            scripts.add(current.toString());
        }
        return scripts;
    }
}
