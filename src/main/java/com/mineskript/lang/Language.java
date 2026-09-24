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

public final class Language {
    public static final String FILE = "/mineskript/lang/english.properties";

    private static final Map<String, String> MESSAGES = load();

    private Language() {
    }

    public static String get(String key) {
        return MESSAGES.getOrDefault(key, key);
    }

    public static String format(String key, Object... arguments) {
        return fill(get(key), arguments);
    }

    public static Set<String> keys() {
        return MESSAGES.keySet();
    }

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
