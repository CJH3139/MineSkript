package com.mineskript.lang.ast;

import java.util.AbstractList;
import java.util.List;

public final class IndexedValues extends AbstractList<Object> {
    private final List<String> indices;
    private final List<Object> values;

    public IndexedValues(List<String> indices, List<Object> values) {
        this.indices = List.copyOf(indices);
        this.values = List.copyOf(values);
    }

    public String index(int position) {
        return indices.get(position);
    }

    @Override
    public Object get(int position) {
        return values.get(position);
    }

    @Override
    public int size() {
        return values.size();
    }
}
