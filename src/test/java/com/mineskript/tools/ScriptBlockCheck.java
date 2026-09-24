package com.mineskript.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ScriptBlockCheck {
    private static final Pattern FENCE = Pattern.compile("```mineskript\\n(.*?)```", Pattern.DOTALL);

    private ScriptBlockCheck() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: ScriptBlockCheck <folder>");
            System.exit(2);
        }
        List<String> failures = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> files = Files.walk(Path.of(args[0]))) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                String name = file.getFileName().toString();
                if (name.endsWith(".md")) {
                    Matcher matcher = FENCE.matcher(Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n"));
                    int n = 0;
                    while (matcher.find()) {
                        checked++;
                        check(file + " block " + (++n), matcher.group(1), failures);
                    }
                } else if (name.endsWith(".json")) {
                    JsonElement root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                    if (!root.isJsonObject()) {
                        continue;
                    }
                    for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
                        if (!entry.getValue().isJsonObject()) {
                            continue;
                        }
                        JsonObject element = entry.getValue().getAsJsonObject();
                        if (!element.has("examples") || !element.get("examples").isJsonArray()) {
                            continue;
                        }
                        int n = 0;
                        for (JsonElement example : element.getAsJsonArray("examples")) {
                            checked++;
                            check(file + " " + entry.getKey() + " example " + (++n), example.getAsString(), failures);
                        }
                    }
                }
            }
        }
        failures.forEach(System.err::println);
        System.out.println("checked " + checked + " scripts, " + failures.size() + " failed");
        if (!failures.isEmpty()) {
            System.exit(1);
        }
    }

    private static void check(String where, String source, List<String> failures) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("example.ms", source);
        for (ParseError error : script.errors()) {
            failures.add(where + ": " + error);
        }
        if (script.errors().isEmpty() && script.triggers().isEmpty() && script.functions().isEmpty()) {
            failures.add(where + ": has no trigger or function");
        }
    }
}
