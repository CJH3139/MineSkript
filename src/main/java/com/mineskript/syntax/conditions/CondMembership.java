package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.AmbiguousExpression;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Comparators;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Relation;
import java.util.List;
import java.util.Optional;

public final class CondMembership implements Condition {
    private final Expression item;
    private final Expression list;
    private final boolean negate;

    private CondMembership(Expression item, Expression list, boolean negate) {
        this.item = item;
        this.list = list;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, 0, 1, false),
                "%object% (is|are) in %objects%");
        registry.addCondition((match, scope) -> create(match, 0, 1, true),
                "%object% (is not|isn't|aren't|are not) in %objects%");
        registry.addCondition((match, scope) -> create(match, 1, 0, false),
                "%objects% (contains|contain) %object%");
        registry.addCondition((match, scope) -> create(match, 1, 0, true),
                "%objects% (doesn't contain|does not contain|don't contain|do not contain) %object%");
    }

    private static Optional<Condition> create(Match match, int itemSlot, int listSlot, boolean negate) {
        Expression item = match.slot(itemSlot);
        Expression list = match.slot(listSlot);
        if (item.isList() || !list.isList()) {
            return Optional.empty();
        }
        SkType listType = list instanceof ListExpression typed ? typed.type() : list.type();
        if (!Comparators.canCompare(item.type(), listType)) {
            Optional<Expression> other = AmbiguousExpression.alternative(item);
            if (other.isEmpty() || !Comparators.canCompare(other.get().type(), listType)) {
                return Optional.empty();
            }
            item = other.get();
        }
        return Optional.of(new CondMembership(item, list, negate));
    }

    @Override
    public boolean test(Context context) {
        Object needle = item.evaluate(context);
        Object haystack = list.evaluate(context);
        boolean result;
        if (haystack instanceof List<?> items) {
            result = items.stream().anyMatch(value -> Comparators.test(Relation.EQUAL, needle, value));
        } else {
            result = Comparators.test(Relation.EQUAL, needle, haystack);
        }
        return negate != result;
    }
}
