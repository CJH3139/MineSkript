package com.mineskript.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record Config(boolean effectCommands, String effectCommandPrefix) {
    public static final String NAME = "config.txt";

    public static final Config DEFAULTS = new Config(true, "?");

    public static final String TEMPLATE = """
            # Lines starting with # are comments.

            # Type an effect into the chat box with this prefix to run it immediately.
            effect commands: true

            # The prefix. Anything you type in chat that starts with it is treated as an effect, not a message.
            effect command prefix: ?
            """;

    public static final char BOM = (char) 0xFEFF;

    public record Loaded(Config config, List<String> warnings, boolean empty) {
    }

    public boolean active() {
        return effectCommands && !effectCommandPrefix.isEmpty();
    }

    public static Loaded parse(String text) {
        return parse(NAME, text);
    }

    public static Loaded parse(String name, String text) {
        boolean effectCommands = DEFAULTS.effectCommands();
        String prefix = DEFAULTS.effectCommandPrefix();
        List<String> warnings = new ArrayList<>();
        Map<String, Integer> applied = new HashMap<>();
        int number = 0;
        boolean empty = true;
        for (String raw : withoutBom(text).lines().toList()) {
            number++;
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            empty = false;
            int colon = line.indexOf(':');
            if (colon < 0) {
                warnings.add(warning(name, number, "has no \":\"", line));
                continue;
            }
            String key = line.substring(0, colon).strip().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).strip();
            switch (key) {
                case "effect commands" -> {
                    if (value.equalsIgnoreCase("true")) {
                        effectCommands = true;
                        repeated(warnings, applied, name, key, number);
                    } else if (value.equalsIgnoreCase("false")) {
                        effectCommands = false;
                        repeated(warnings, applied, name, key, number);
                    } else {
                        warnings.add(warning(name, number, "wants true or false", line));
                    }
                }
                case "effect command prefix" -> {
                    prefix = value;
                    repeated(warnings, applied, name, key, number);
                    if (value.isEmpty()) {
                        warnings.add(kept(name, number, "has an empty prefix, so effect commands are off", line));
                    } else if (value.startsWith("/")) {
                        warnings.add(kept(name, number, "has a prefix starting with \"/\", which the chat box sends as a command, so it never reaches MineSkript", line));
                    }
                }
                default -> warnings.add(warning(name, number, "is not a setting MineSkript knows", line));
            }
        }
        return new Loaded(new Config(effectCommands, prefix), List.copyOf(warnings), empty);
    }

    private static String withoutBom(String text) {
        return !text.isEmpty() && text.charAt(0) == BOM ? text.substring(1) : text;
    }

    private static void repeated(List<String> warnings, Map<String, Integer> applied, String name, String key, int number) {
        Integer before = applied.put(key, number);
        if (before != null) {
            warnings.add(name + " line " + number + " sets \"" + key + "\" again, so line " + number
                    + " is the one that counts, not line " + before);
        }
    }

    private static String warning(String name, int number, String problem, String line) {
        return name + " line " + number + " " + problem + ", ignored: " + line;
    }

    private static String kept(String name, int number, String problem, String line) {
        return name + " line " + number + " " + problem + ", applied as written: " + line;
    }
}
