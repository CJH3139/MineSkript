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
        List<ParsedScript> scripts = new ArrayList<>();
        try {
            Files.createDirectories(dir);
            List<Path> files = listScripts(dir);
            if (files.isEmpty()) {
                Files.writeString(dir.resolve("example.ms"), EXAMPLE, StandardCharsets.UTF_8);
                files = listScripts(dir);
            }
            for (Path file : files) {
                scripts.add(parseFile(file));
            }
        } catch (IOException error) {
            scripts.add(new ParsedScript(dir.toString(), List.of(), List.of(new ParseError(dir.toString(), 0, "cannot read scripts folder: " + error.getMessage()))));
        }
        return new LoadReport(List.copyOf(scripts));
    }

    private ParsedScript parseFile(Path file) {
        String name = file.getFileName().toString();
        try {
            return parser.parse(name, Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException error) {
            return new ParsedScript(name, List.of(), List.of(new ParseError(name, 0, "cannot read file: " + error.getMessage())));
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
