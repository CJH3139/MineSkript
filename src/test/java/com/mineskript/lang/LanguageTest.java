package com.mineskript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LanguageTest {
    private static final Path SOURCES = Path.of("src/main/java");
    private static final List<String> CALLS = List.of("Language.get(", "Language.format(");

    @Test
    void theFileLoadsFromTheClasspath() {
        assertFalse(Language.keys().isEmpty());
        assertEquals("unexpected indentation", Language.get("lexer.unexpected-indentation"));
    }

    @Test
    void everyKeyTheCodeUsesIsInTheFileAndEveryKeyInTheFileIsUsed() throws IOException {
        Set<String> used = usedKeys();
        Set<String> missing = new TreeSet<>(used);
        missing.removeAll(Language.keys());
        assertEquals(Set.of(), missing, "keys used in code but missing from " + Language.FILE);
        Set<String> unused = new TreeSet<>(Language.keys());
        unused.removeAll(used);
        assertEquals(Set.of(), unused, "keys in " + Language.FILE + " that no code uses");
    }

    @Test
    void placeholdersAreFilledInOrderAndNothingElseIsTouched() {
        assertEquals("function \"f\" is already defined on line 3",
                Language.format("parse.function-defined-on-line", "f", 3));
        assertEquals("a {x} b {9}", Language.fill("{0} {x} {1} {9}", "a", "b"));
        assertEquals("it's {0}", Language.fill("it's {0}"));
        assertEquals("{} {01x}", Language.fill("{} {01x}", "a"));
        assertEquals("type an effect after ? to run it, like ?send \"hello\"", Language.format("effect-command.empty", "?"));
    }

    @Test
    void anArgumentIsNeverReadAsAPlaceholder() {
        assertEquals("unknown effect \"set {0} to {1}\"", Language.format("parse.unknown-effect", "set {0} to {1}", "x"));
    }

    @Test
    void aMissingKeyGivesTheKeyBack() {
        assertEquals("no.such.key", Language.get("no.such.key"));
        assertEquals("no.such.key", Language.format("no.such.key", 1, 2));
    }

    @Test
    void specialCharactersSurviveTheFile() {
        assertEquals("the last line ends with \"\\\" but nothing follows it", Language.get("lexer.dangling-continuation"));
        assertEquals("unterminated % in string", Language.get("parse.unterminated-percent"));
        assertEquals("function \"f\" doesn't return a value, add \":: type\" to its header",
                Language.format("parse.function-returns-no-value", "f"));
    }

    @Test
    void theLanguageLayerHasNoMinecraftImports() throws IOException {
        String source = Files.readString(SOURCES.resolve("com/mineskript/lang/Language.java"), StandardCharsets.UTF_8);
        assertFalse(source.contains("net.minecraft"));
        assertFalse(source.contains("net.fabricmc"));
    }

    private static Set<String> usedKeys() throws IOException {
        Set<String> keys = new TreeSet<>();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(SOURCES)) {
            files = walk.filter(path -> path.toString().endsWith(".java")).toList();
        }
        for (Path file : files) {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            for (String call : CALLS) {
                int at = source.indexOf(call);
                while (at >= 0) {
                    keys.addAll(literals(firstArgument(source, at + call.length())));
                    at = source.indexOf(call, at + 1);
                }
            }
        }
        assertTrue(keys.size() > 50, "found only " + keys);
        return keys;
    }

    private static String firstArgument(String source, int start) {
        int depth = 0;
        boolean quoted = false;
        for (int i = start; i < source.length(); i++) {
            char c = source.charAt(i);
            if (quoted) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    quoted = false;
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')' && depth == 0 || c == ',' && depth == 0) {
                return source.substring(start, i);
            } else if (c == ')') {
                depth--;
            }
        }
        throw new AssertionError("unclosed Language call");
    }

    private static List<String> literals(String text) {
        List<String> found = new ArrayList<>();
        int open = text.indexOf('"');
        while (open >= 0) {
            int close = text.indexOf('"', open + 1);
            found.add(text.substring(open + 1, close));
            open = text.indexOf('"', close + 1);
        }
        return found;
    }
}
