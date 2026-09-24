package com.mineskript.lang.ast;

import java.util.ArrayList;
import java.util.List;

public final class TooltipLines {
    public static final String KEY = "tooltip lines";
    public static final int MAX_LINES = 64;

    private final List<String> top = new ArrayList<>();
    private final List<String> bottom = new ArrayList<>();

    public void addTop(String line) {
        if (size() < MAX_LINES) {
            top.add(line);
        }
    }

    public void addBottom(String line) {
        if (size() < MAX_LINES) {
            bottom.add(line);
        }
    }

    public List<String> top() {
        return List.copyOf(top);
    }

    public List<String> bottom() {
        return List.copyOf(bottom);
    }

    public boolean isEmpty() {
        return top.isEmpty() && bottom.isEmpty();
    }

    private int size() {
        return top.size() + bottom.size();
    }
}
