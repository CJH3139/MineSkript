package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Functions {
    private Map<String, List<Function>> loaded = Map.of();
    private final Map<String, List<Function>> declared = new HashMap<>();

    public void replace(Map<String, List<Function>> byFile) {
        Map<String, List<Function>> copy = new HashMap<>();
        byFile.forEach((file, functions) -> copy.put(file, List.copyOf(functions)));
        loaded = copy;
    }

    public void declare(String file, List<Function> functions) {
        declared.put(file, List.copyOf(functions));
    }

    public void forgetDeclared() {
        declared.clear();
    }

    public Optional<Function> visibleFrom(String name, String file) {
        return find(declared.isEmpty() ? loaded : declared, name, file);
    }

    public Optional<Function> clashWith(String name, String file) {
        if (declared.isEmpty()) {
            return find(loaded, name, file);
        }
        Map<String, List<Function>> earlier = new HashMap<>();
        declared.forEach((other, functions) -> {
            if (other.compareTo(file) < 0) {
                earlier.put(other, functions);
            }
        });
        return find(earlier, name, file);
    }

    public Optional<Function> loaded(String name, String file) {
        return find(loaded, name, file);
    }

    private static Optional<Function> find(Map<String, List<Function>> source, String name, String file) {
        for (Map.Entry<String, List<Function>> entry : source.entrySet()) {
            if (entry.getKey().equals(file)) {
                continue;
            }
            for (Function function : entry.getValue()) {
                if (!function.local() && function.name().equals(name)) {
                    return Optional.of(function);
                }
            }
        }
        return Optional.empty();
    }
}
