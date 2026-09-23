package com.mineskript.script;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class ScriptLoader {
    public static final String EXAMPLE = """
            on key press of "r":
                if block below player is stone or cobblestone:
                    send "on stone, mining at y %player's y-coordinate%"
                    hold attack
                    wait 20 ticks
                    release attack
                else:
                    send "not stone: %block below player%"

            every 5 seconds:
                if health of player is less than 6:
                    make player say "low hp!"
            """;

    private final Parser parser;

    public ScriptLoader(Parser parser) {
        this.parser = parser;
    }

    public LoadReport load(Path dir) {
        return load(dir, true, new ScriptSources(dir));
    }

    public LoadReport load(Path dir, boolean seedExample, ScriptSources sources) {
        List<ParsedScript> scripts = new ArrayList<>();
        sources.forget();
        try {
            Files.createDirectories(dir);
            List<Path> files = listScripts(dir);
            if (files.isEmpty() && seedExample) {
                Files.writeString(dir.resolve("example.ms"), EXAMPLE, StandardCharsets.UTF_8);
                files = listScripts(dir);
            }
            for (Path file : files) {
                scripts.add(parseFile(file, sources));
            }
        } catch (IOException error) {
            scripts.add(new ParsedScript(dir.toString(), List.of(), List.of(new ParseError(dir.toString(), 0, "cannot read scripts folder: " + error.getMessage()))));
        }
        return new LoadReport(List.copyOf(scripts));
    }

    public static boolean createFolder(Path dir) {
        try {
            if (Files.isDirectory(dir)) {
                return false;
            }
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("example.ms"), EXAMPLE, StandardCharsets.UTF_8);
            return true;
        } catch (IOException error) {
            return false;
        }
    }

    public static boolean isScriptName(String file) {
        return file.endsWith(".ms") && !file.contains("/") && !file.contains("\\");
    }

    public Optional<ParsedScript> loadOne(Path dir, String file) {
        return loadOne(dir, file, new ScriptSources(dir));
    }

    public Optional<ParsedScript> loadOne(Path dir, String file, ScriptSources sources) {
        if (!isScriptName(file)) {
            return Optional.empty();
        }
        Path path = dir.resolve(file);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        return Optional.of(parseFile(path, sources));
    }

    private ParsedScript parseFile(Path file, ScriptSources sources) {
        String name = file.getFileName().toString();
        try {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            ParsedScript script = parser.parse(name, text);
            sources.capture(name, text, script.errors());
            return script;
        } catch (IOException error) {
            ParsedScript script = new ParsedScript(name, List.of(), List.of(new ParseError(name, 0, "cannot read file: " + error.getMessage())));
            sources.capture(name, "", script.errors());
            return script;
        }
    }

    private static List<Path> listScripts(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".ms"))
                    .sorted((a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()))
                    .toList();
        }
    }
}
