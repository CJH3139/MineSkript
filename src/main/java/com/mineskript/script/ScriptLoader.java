package com.mineskript.script;

import com.mineskript.lang.Language;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private record Source(String text, String error) {
    }

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
            Map<Path, Source> texts = new LinkedHashMap<>();
            for (Path file : files) {
                texts.put(file, read(file));
            }
            texts.forEach((file, source) -> {
                if (source.text() != null) {
                    parser.declare(ScriptNames.nameOf(dir, file), source.text());
                }
            });
            for (Path file : files) {
                scripts.add(parse(ScriptNames.nameOf(dir, file), texts.get(file), sources));
            }
        } catch (IOException error) {
            scripts.add(new ParsedScript(dir.toString(), List.of(), List.of(new ParseError(dir.toString(), 0, Language.format("loader.cannot-read-folder", error.getMessage())))));
        } finally {
            parser.functions().forgetDeclared();
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
        return ScriptNames.isScriptName(file);
    }

    public Optional<ParsedScript> loadOne(Path dir, String file) {
        return loadOne(dir, file, new ScriptSources(dir));
    }

    public Optional<ParsedScript> loadOne(Path dir, String file, ScriptSources sources) {
        Optional<Path> path = ScriptNames.resolve(dir, file);
        if (path.isEmpty() || !Files.isRegularFile(path.get())) {
            return Optional.empty();
        }
        return Optional.of(parse(ScriptNames.normalize(file), read(path.get()), sources));
    }

    private static Source read(Path file) {
        try {
            return new Source(Files.readString(file, StandardCharsets.UTF_8), null);
        } catch (IOException error) {
            return new Source(null, error.getMessage());
        }
    }

    private ParsedScript parse(String name, Source source, ScriptSources sources) {
        if (source.text() == null) {
            ParsedScript script = new ParsedScript(name, List.of(), List.of(new ParseError(name, 0, Language.format("loader.cannot-read-file", source.error()))));
            sources.capture(name, "", script.errors());
            return script;
        }
        ParsedScript script = parser.parse(name, source.text());
        sources.capture(name, source.text(), script.errors());
        return script;
    }

    private static List<Path> listScripts(Path dir) throws IOException {
        try {
            return ScriptNames.files(dir);
        } catch (UncheckedIOException error) {
            throw error.getCause();
        }
    }
}
