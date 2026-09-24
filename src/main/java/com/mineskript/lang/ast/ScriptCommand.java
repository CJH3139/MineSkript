package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.TextPattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ScriptCommand(String name, List<String> aliases, List<Argument> arguments, TextPattern pattern,
        String usage, String description, int cooldownTicks, String cooldownMessage) {
    public static final String ARGUMENTS = "arguments";
    public static final String LABEL = "command label";

    public record Argument(TextPattern.Kind kind, boolean plural, boolean optional, Object fallback) {
        public SkType type() {
            return kind.type();
        }
    }

    public ScriptCommand {
        aliases = List.copyOf(aliases);
        arguments = List.copyOf(arguments);
    }

    public List<String> labels() {
        List<String> labels = new ArrayList<>();
        labels.add(name);
        labels.addAll(aliases);
        return List.copyOf(labels);
    }

    public Optional<List<Object>> parse(String input) {
        String text = input.strip();
        if (pattern == null) {
            return text.isEmpty() ? Optional.of(List.of()) : Optional.empty();
        }
        Optional<List<Object>> slots = pattern.matchSlots(text);
        if (slots.isEmpty()) {
            return Optional.empty();
        }
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            Object value = slots.get().get(i);
            Object fallback = arguments.get(i).fallback();
            values.add(value != null ? value : fallback != null ? fallback : None.NONE);
        }
        return Optional.of(List.copyOf(values));
    }
}
