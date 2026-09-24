package com.mineskript.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * The messages MineSkript shows to players and script writers, read once from {@value #FILE} on the classpath, the
 * way Skript keeps its messages in {@code english.lang}.
 *
 * <p>A message may hold numbered placeholders, {@code {0}}, {@code {1}} and so on, which {@link #format} fills with
 * its arguments in order. Nothing else in a message is special: quotes, apostrophes and other braces are kept as
 * written. A key missing from the file gives back the key itself, so a missing message shows up plainly instead of
 * crashing the game.
 *
 * <p>This class must not import Minecraft, so the parser, the interpreter and the tests can use it anywhere.
 */
public final class Language {
    /** Where the English messages live on the classpath. */
    public static final String FILE = "/mineskript/lang/english.properties";

    private static final Map<String, String> MESSAGES = load();

    private Language() {
    }

    /** The message with this key, or the key itself when the file has no such message. */
    public static String get(String key) {
        return MESSAGES.getOrDefault(key, key);
    }

    /** The message with this key, its placeholders filled with the arguments in order. */
    public static String format(String key, Object... arguments) {
        return fill(get(key), arguments);
    }

    /** Every key in the file, sorted. */
    public static Set<String> keys() {
        return MESSAGES.keySet();
    }

    /**
     * Replaces each {@code {n}} in the template with the n-th argument. A placeholder with no matching argument,
     * and anything else in braces, stays as it is.
     */
    static String fill(String template, Object... arguments) {
        StringBuilder result = new StringBuilder(template.length() + 16);
        int i = 0;
        while (i < template.length()) {
            char c = template.charAt(i);
            int close = c == '{' ? placeholderEnd(template, i) : -1;
            if (close < 0) {
                result.append(c);
                i++;
                continue;
            }
            int index = Integer.parseInt(template.substring(i + 1, close));
            if (index < arguments.length) {
                result.append(arguments[index]);
            } else {
                result.append(template, i, close + 1);
            }
            i = close + 1;
        }
        return result.toString();
    }

    /** Where the {@code {n}} starting at {@code open} ends, or -1 when it is not a placeholder. */
    private static int placeholderEnd(String template, int open) {
        int i = open + 1;
        while (i < template.length() && i - open <= 2 && Character.isDigit(template.charAt(i))) {
            i++;
        }
        return i > open + 1 && i < template.length() && template.charAt(i) == '}' ? i : -1;
    }

    private static Map<String, String> load() {
        Properties properties = new Properties();
        try (InputStream stream = Language.class.getResourceAsStream(FILE)) {
            if (stream == null) {
                return Map.of();
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException error) {
            return Map.of();
        }
        TreeMap<String, String> messages = new TreeMap<>();
        for (String key : properties.stringPropertyNames()) {
            messages.put(key, properties.getProperty(key));
        }
        return Collections.unmodifiableSortedMap(messages);
    }
}
