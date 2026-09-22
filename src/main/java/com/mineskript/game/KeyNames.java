package com.mineskript.game;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.SyntaxException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class KeyNames {
    private static final Set<String> KEYBOARD = keyboardNames();
    private static final Set<String> MOUSE = Set.of("left", "right", "middle", "4", "5", "6", "7", "8");
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("shift", "left.shift"),
            Map.entry("lshift", "left.shift"),
            Map.entry("rshift", "right.shift"),
            Map.entry("ctrl", "left.control"),
            Map.entry("control", "left.control"),
            Map.entry("lctrl", "left.control"),
            Map.entry("rctrl", "right.control"),
            Map.entry("alt", "left.alt"),
            Map.entry("lalt", "left.alt"),
            Map.entry("ralt", "right.alt"),
            Map.entry("esc", "escape"),
            Map.entry("return", "enter"),
            Map.entry("pageup", "page.up"),
            Map.entry("pagedown", "page.down"),
            Map.entry("capslock", "caps.lock"));

    private KeyNames() {
    }

    public static Optional<String> toKeyId(String name) {
        String normalised = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", ".");
        if (normalised.isEmpty()) {
            return Optional.empty();
        }
        if (normalised.startsWith("mouse.")) {
            String button = normalised.substring("mouse.".length());
            return MOUSE.contains(button) ? Optional.of("key.mouse." + button) : Optional.empty();
        }
        String key = ALIASES.getOrDefault(normalised, normalised);
        return KEYBOARD.contains(key) ? Optional.of("key.keyboard." + key) : Optional.empty();
    }

    public static String keyIdOf(Expression expression) {
        if (!(expression instanceof ConstantExpression constant) || !(constant.value() instanceof String name)) {
            throw new SyntaxException("key name must be a plain string like \"r\"");
        }
        return toKeyId(name).orElseThrow(() -> new SyntaxException("unknown key \"" + name + "\""));
    }

    private static Set<String> keyboardNames() {
        Set<String> names = new HashSet<>();
        for (char c = 'a'; c <= 'z'; c++) {
            names.add(String.valueOf(c));
        }
        for (int i = 0; i <= 9; i++) {
            names.add(String.valueOf(i));
            names.add("keypad." + i);
        }
        for (int i = 1; i <= 25; i++) {
            names.add("f" + i);
        }
        names.addAll(Set.of(
                "space", "enter", "escape", "tab", "backspace", "insert", "delete",
                "right", "left", "up", "down", "page.up", "page.down", "home", "end",
                "caps.lock", "scroll.lock", "num.lock", "print.screen", "pause",
                "keypad.decimal", "keypad.divide", "keypad.multiply", "keypad.subtract", "keypad.add", "keypad.enter", "keypad.equal",
                "left.shift", "right.shift", "left.control", "right.control", "left.alt", "right.alt", "left.win", "right.win", "menu",
                "apostrophe", "comma", "minus", "period", "slash", "semicolon", "equal",
                "left.bracket", "backslash", "right.bracket", "grave.accent", "world.1", "world.2"));
        return Set.copyOf(names);
    }
}
