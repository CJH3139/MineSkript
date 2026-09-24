package com.mineskript.script;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

public final class ScriptNames {
    private ScriptNames() {
    }

    public record Listing(List<String> names, boolean readable) {
    }

    public static List<String> list(Path dir) {
        return listing(dir).names();
    }

    public static Listing listing(Path dir) {
        try {
            return new Listing(files(dir).stream().map(file -> nameOf(dir, file)).toList(), true);
        } catch (IOException | UncheckedIOException error) {
            return new Listing(List.of(), false);
        }
    }

    public static List<Path> files(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IOException(dir + " is not a folder");
        }
        try (Stream<Path> entries = Files.walk(dir)) {
            return entries
                    .filter(path -> Files.isRegularFile(path) && enabled(dir, path)
                            && path.getFileName().toString().endsWith(".ms"))
                    .sorted(Comparator.comparing(path -> nameOf(dir, path)))
                    .toList();
        }
    }

    public static String nameOf(Path dir, Path file) {
        List<String> parts = new ArrayList<>();
        for (Path part : dir.relativize(file)) {
            parts.add(part.toString());
        }
        return String.join("/", parts);
    }

    public static String normalize(String typed) {
        return typed.replace('\\', '/');
    }

    public static boolean isScriptName(String name) {
        String normal = normalize(name);
        if (!normal.endsWith(".ms") || normal.contains(":")) {
            return false;
        }
        for (String part : normal.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..") || part.startsWith("-")) {
                return false;
            }
        }
        return true;
    }

    public static Optional<Path> resolve(Path dir, String name) {
        if (!isScriptName(name)) {
            return Optional.empty();
        }
        Path base = dir.toAbsolutePath().normalize();
        Path file = base.resolve(normalize(name)).normalize();
        return file.startsWith(base) ? Optional.of(file) : Optional.empty();
    }

    private static boolean enabled(Path dir, Path file) {
        for (Path part : dir.relativize(file)) {
            if (part.toString().startsWith("-")) {
                return false;
            }
        }
        return true;
    }

    public static List<String> matching(Path dir, String typed) {
        return matching(dir, List.of(), typed);
    }

    public static List<String> matching(Path dir, Collection<String> loaded, String typed) {
        String needle = normalize(typed).toLowerCase(Locale.ROOT);
        TreeSet<String> everyName = new TreeSet<>(list(dir));
        for (String name : loaded) {
            if (name.endsWith(".ms")) {
                everyName.add(name);
            }
        }
        List<String> names = new ArrayList<>();
        for (String name : everyName) {
            if (name.toLowerCase(Locale.ROOT).startsWith(needle)) {
                names.add(name);
            }
        }
        return List.copyOf(names);
    }

    public static Optional<String> onDisk(Path dir, String typed) {
        return pick(list(dir), typed);
    }

    public static Optional<String> pick(List<String> names, String typed) {
        typed = normalize(typed);
        if (names.contains(typed)) {
            return Optional.of(typed);
        }
        for (String name : names) {
            if (name.equalsIgnoreCase(typed)) {
                return Optional.of(name);
            }
        }
        return Optional.empty();
    }
}
