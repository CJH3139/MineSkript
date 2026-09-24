package com.mineskript.script;

import com.mineskript.lang.Language;
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
                warnings.add(warning(name, number, Language.get("config.no-colon"), line));
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
                        warnings.add(warning(name, number, Language.get("config.wants-boolean"), line));
                    }
                }
                case "effect command prefix" -> {
                    prefix = value;
                    repeated(warnings, applied, name, key, number);
                    if (value.isEmpty()) {
                        warnings.add(kept(name, number, Language.get("config.empty-prefix"), line));
                    } else if (value.startsWith("/")) {
                        warnings.add(kept(name, number, Language.get("config.slash-prefix"), line));
                    }
                }
                default -> warnings.add(warning(name, number, Language.get("config.unknown-setting"), line));
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
            warnings.add(Language.format("config.set-again", name, number, key, before));
        }
    }

    private static String warning(String name, int number, String problem, String line) {
        return Language.format("config.ignored", name, number, problem, line);
    }

    private static String kept(String name, int number, String problem, String line) {
        return Language.format("config.applied", name, number, problem, line);
    }
}
