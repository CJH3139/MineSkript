package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import java.util.List;
import java.util.Set;

public record Match(List<Expression> slots, Set<String> tags, int patternIndex) {
    public Expression slot(int index) {
        return slots.get(index);
    }

    public boolean has(String tag) {
        return tags.contains(tag);
    }
}
