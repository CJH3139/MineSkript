package com.mineskript.lang.ast;

import java.util.List;
import java.util.Map;

public record EventFilter(String value, List<Object> accepted) {
    public static final EventFilter ANY = new EventFilter("", List.of());

    public EventFilter {
        accepted = List.copyOf(accepted);
    }

    public boolean accepts(Map<String, Object> values) {
        if (accepted.isEmpty()) {
            return true;
        }
        Object actual = values.get(value);
        return accepted.stream().anyMatch(expected -> matches(expected, actual));
    }

    private static boolean matches(Object expected, Object actual) {
        return switch (expected) {
            case BlockType type -> actual instanceof BlockType block && block.id().equals(type.id())
                    || actual instanceof ItemValue item && item.id().equals(type.id());
            case EntityType type -> actual instanceof EntityValue entity && entity.id().equals(type.id());
            default -> expected.equals(actual);
        };
    }
}
