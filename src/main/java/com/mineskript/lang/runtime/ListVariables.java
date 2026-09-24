package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.IndexedValues;
import com.mineskript.lang.ast.None;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ListVariables {
    static final String SEPARATOR = "::";

    private ListVariables() {
    }

    static IndexedValues read(Map<String, Object> map, String prefix) {
        String start = prefix + SEPARATOR;
        List<String> indices = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(start) || entry.getValue() == None.NONE) {
                continue;
            }
            String index = key.substring(start.length());
            if (!index.isEmpty() && !index.contains(SEPARATOR)) {
                indices.add(index);
                values.add(entry.getValue());
            }
        }
        return new IndexedValues(indices, values);
    }

    static boolean delete(Map<String, Object> map, String prefix) {
        String start = prefix + SEPARATOR;
        return map.keySet().removeIf(key -> key.startsWith(start));
    }
}
