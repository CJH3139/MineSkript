package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondHasItem implements Condition {
    private final Expression item;
    private final boolean holding;
    private final boolean negate;

    private CondHasItem(Expression item, boolean holding, boolean negate) {
        this.item = item;
        this.holding = holding;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, false, false),
                "%player% (has|have) %blocktype%");
        registry.addCondition((match, scope) -> create(match, false, true),
                "%player% (doesn't have|does not have|don't have|do not have) %blocktype%");
        registry.addCondition((match, scope) -> create(match, true, false),
                "%player% (is|are) holding %blocktype%");
        registry.addCondition((match, scope) -> create(match, true, true),
                "%player% (isn't|is not|aren't|are not) holding %blocktype%");
    }

    private static Optional<Condition> create(Match match, boolean holding, boolean negate) {
        Expression item = match.slot(1);
        if (item.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondHasItem(item, holding, negate));
    }

    @Override
    public boolean test(Context context) {
        Object value = item.evaluate(context);
        boolean result = false;
        if (value instanceof BlockType type) {
            result = holding
                    ? context.world().heldItem().id().equals(type.id())
                    : context.world().countItem(type.id()) > 0;
        }
        return negate != result;
    }
}
