package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.IndexedValues;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Variables {
    private final Map<String, Object> global = new LinkedHashMap<>();
    private final Map<String, Object> ram = new LinkedHashMap<>();
    private int version;

    public Map<String, Object> global() {
        return global;
    }

    public Map<String, Object> ram() {
        return ram;
    }

    public int version() {
        return version;
    }

    public void replaceGlobals(Map<String, Object> values) {
        global.clear();
        global.putAll(values);
    }

    Object get(VariableScope scope, String name) {
        return map(scope).get(name);
    }

    void set(VariableScope scope, String name, Object value) {
        map(scope).put(name, value);
        if (scope == VariableScope.GLOBAL) {
            version++;
        }
    }

    void delete(VariableScope scope, String name) {
        map(scope).remove(name);
        if (scope == VariableScope.GLOBAL) {
            version++;
        }
    }

    IndexedValues list(VariableScope scope, String prefix) {
        return ListVariables.read(map(scope), prefix);
    }

    void deleteList(VariableScope scope, String prefix) {
        if (ListVariables.delete(map(scope), prefix) && scope == VariableScope.GLOBAL) {
            version++;
        }
    }

    private Map<String, Object> map(VariableScope scope) {
        return scope == VariableScope.RAM ? ram : global;
    }
}
