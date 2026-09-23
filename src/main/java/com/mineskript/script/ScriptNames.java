package com.mineskript.script;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
        try (Stream<Path> entries = Files.list(dir)) {
            return new Listing(entries
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".ms"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList(), true);
        } catch (IOException error) {
            return new Listing(List.of(), false);
        }
    }

    public static List<String> matching(Path dir, String typed) {
        return matching(dir, List.of(), typed);
    }

    public static List<String> matching(Path dir, Collection<String> loaded, String typed) {
        String needle = typed.toLowerCase(Locale.ROOT);
        TreeSet<String> everyName = new TreeSet<>(list(dir));
        for (String name : loaded) {
            if (name.endsWith(".ms")) {
                everyName.add(name);
            }
        }
        List<String> names = new ArrayList<>();
        for (String name : everyName) {
            if (typeable(name) && name.toLowerCase(Locale.ROOT).startsWith(needle)) {
                names.add(name);
            }
        }
        return List.copyOf(names);
    }

    public static Optional<String> onDisk(Path dir, String typed) {
        return pick(list(dir), typed);
    }

    public static Optional<String> pick(List<String> names, String typed) {
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

    private static boolean typeable(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean allowed = (c >= '0' && c <= '9')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || c == '_' || c == '-' || c == '.' || c == '+';
            if (!allowed) {
                return false;
            }
        }
        return true;
    }
}
