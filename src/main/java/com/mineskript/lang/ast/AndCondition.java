package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.List;

public record AndCondition(List<Condition> conditions) implements Condition {
    @Override
    public boolean test(Context context) {
        for (Condition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }
}
