package com.mineskript.syntax.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.AmbiguousExpression;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Comparators;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Relation;
import com.mineskript.lang.runtime.ScriptError;
import java.util.List;
import java.util.Optional;

public final class CondCompare implements Condition {
    private final Expression left;
    private final Expression right;
    private final Expression high;
    private final Relation relation;
    private final boolean negate;
    private final boolean disjunctive;

    private CondCompare(Expression left, Expression right, Expression high, Relation relation, boolean negate, boolean disjunctive) {
        this.left = left;
        this.right = right;
        this.high = high;
        this.relation = relation;
        this.negate = negate;
        this.disjunctive = disjunctive;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, Relation.EQUAL, false),
                "%objects% (is|are|=) [(equal to|the same as)] %objects%");
        registry.addCondition((match, scope) -> create(match, Relation.EQUAL, true),
                "%objects% (is not|isn't|aren't|are not|!=) [equal to] %objects%");
        registry.addCondition((match, scope) -> create(match, Relation.GREATER_OR_EQUAL, false),
                "%objects% (is|are) ((greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|at least) %objects%",
                "%objects% >= %objects%");
        registry.addCondition((match, scope) -> create(match, Relation.LESS_OR_EQUAL, false),
                "%objects% (is|are) ((less|smaller|lower|below) [than] or (equal to|the same as)|at most) %objects%",
                "%objects% <= %objects%");
        registry.addCondition((match, scope) -> create(match, Relation.GREATER, false),
                "%objects% (is|are) ((greater|more|higher|bigger|larger) than|above|>) %objects%",
                "%objects% > %objects%");
        registry.addCondition((match, scope) -> create(match, Relation.LESS, false),
                "%objects% (is|are) ((less|smaller|lower) than|below|<) %objects%",
                "%objects% < %objects%");
        registry.addCondition((match, scope) -> createBetween(match, false),
                "%objects% (is|are) between %objects% and %objects%");
        registry.addCondition((match, scope) -> createBetween(match, true),
                "%objects% (is not|isn't|aren't|are not) between %objects% and %objects%");
    }

    private static Optional<Condition> create(Match match, Relation relation, boolean negate) {
        Expression left = match.slot(0);
        Expression right = match.slot(1);
        if (left.isList()) {
            return Optional.empty();
        }
        SkType rightType = right instanceof ListExpression list ? list.type() : right.type();
        if (!Comparators.canCompare(left.type(), rightType)) {
            Optional<Expression> other = AmbiguousExpression.alternative(right);
            if (other.isEmpty() || !Comparators.canCompare(left.type(), other.get().type())) {
                return Optional.empty();
            }
            right = other.get();
            rightType = right.type();
        }
        if (relation.ordered() && !Comparators.canOrder(left.type(), rightType)) {
            return Optional.empty();
        }
        boolean disjunctive = right instanceof ListExpression list && list.disjunctive();
        return Optional.of(new CondCompare(left, right, null, relation, negate, disjunctive));
    }

    private static Optional<Condition> createBetween(Match match, boolean negate) {
        Expression left = match.slot(0);
        Expression low = match.slot(1);
        Expression high = match.slot(2);
        if (left.isList() || low.isList() || high.isList()) {
            return Optional.empty();
        }
        if (!Comparators.canOrder(left.type(), low.type()) || !Comparators.canOrder(left.type(), high.type())) {
            return Optional.empty();
        }
        return Optional.of(new CondCompare(left, low, high, Relation.EQUAL, negate, false));
    }

    @Override
    public boolean test(Context context) {
        Object leftValue = left.evaluate(context);
        if (leftValue instanceof List<?>) {
            throw new ScriptError("the left side of a comparison cannot be a list");
        }
        boolean result;
        if (high != null) {
            Object lowValue = right.evaluate(context);
            Object highValue = high.evaluate(context);
            if (leftValue == None.NONE || lowValue == None.NONE || highValue == None.NONE) {
                result = false;
            } else {
                result = Comparators.relate(leftValue, lowValue) >= 0 && Comparators.relate(leftValue, highValue) <= 0;
            }
        } else {
            Object rightValue = right.evaluate(context);
            if (rightValue instanceof List<?> items) {
                result = !items.isEmpty() && (disjunctive
                        ? items.stream().anyMatch(item -> Comparators.test(relation, leftValue, item))
                        : items.stream().allMatch(item -> Comparators.test(relation, leftValue, item)));
            } else {
                result = Comparators.test(relation, leftValue, rightValue);
            }
        }
        return negate != result;
    }
}
