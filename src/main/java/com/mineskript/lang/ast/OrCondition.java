package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.List;

public record OrCondition(List<Condition> conditions) implements Condition {
    @Override
    public boolean test(Context context) {
        for (Condition condition : conditions) {
            if (condition.test(context)) {
                return true;
            }
        }
        return false;
    }
}
