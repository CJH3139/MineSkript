package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

public final class CondIsSet implements Condition {
    private final Expression value;
    private final boolean negate;

    private CondIsSet(Expression value, boolean negate) {
        this.value = value;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> Optional.of(new CondIsSet(match.slot(0), false)), "%objects% (is|are) set");
        registry.addCondition((match, scope) -> Optional.of(new CondIsSet(match.slot(0), true)), "%objects% (isn't|is not|aren't|are not) set");
    }

    @Override
    public boolean test(Context context) {
        return negate != isSet(value.evaluate(context));
    }

    private static boolean isSet(Object value) {
        if (value instanceof List<?> list) {
            return !list.isEmpty() && list.stream().allMatch(CondIsSet::isSet);
        }
        return value != null && value != None.NONE;
    }
}
