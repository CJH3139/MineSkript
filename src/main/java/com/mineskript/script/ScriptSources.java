package com.mineskript.script;

import com.mineskript.lang.ParseError;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScriptSources {
    private final Path dir;
    private final Map<String, Map<Integer, String>> captured = new LinkedHashMap<>();

    public ScriptSources(Path dir) {
        this.dir = dir;
    }

    public void forget() {
        captured.clear();
    }

    public void capture(String file, String text, List<ParseError> errors) {
        Map<Integer, String> lines = new LinkedHashMap<>();
        if (!errors.isEmpty()) {
            List<String> source = text.lines().toList();
            for (ParseError error : errors) {
                int number = error.line();
                if (number >= 1 && number <= source.size()) {
                    lines.put(number, source.get(number - 1).strip());
                }
            }
        }
        captured.put(file, Map.copyOf(lines));
    }

    public String line(String file, int number) {
        Map<Integer, String> lines = captured.get(file);
        if (lines != null) {
            String text = lines.get(number);
            return text == null ? "" : text;
        }
        return fromDisk(file, number);
    }

    private String fromDisk(String file, int number) {
        if (number <= 0 || file.contains("/") || file.contains("\\")) {
            return "";
        }
        try {
            List<String> lines = Files.readAllLines(dir.resolve(file), StandardCharsets.UTF_8);
            if (number > lines.size()) {
                return "";
            }
            return lines.get(number - 1).strip();
        } catch (IOException | RuntimeException error) {
            return "";
        }
    }
}
