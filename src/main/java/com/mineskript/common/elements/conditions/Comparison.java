package com.mineskript.common.elements.conditions;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.AmbiguousExpression;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Literals;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.runtime.Comparators;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Relation;
import com.mineskript.lang.runtime.ScriptError;
import java.util.List;
import java.util.Optional;

final class Comparison implements Condition {
    private final Expression left;
    private final Expression right;
    private final Expression high;
    private final Relation relation;
    private final boolean negate;
    private final boolean disjunctive;

    private Comparison(Expression left, Expression right, Expression high, Relation relation, boolean negate, boolean disjunctive) {
        this.left = left;
        this.right = right;
        this.high = high;
        this.relation = relation;
        this.negate = negate;
        this.disjunctive = disjunctive;
    }

    static Optional<Condition> create(Match match, Relation relation, boolean negate) {
        Expression left = match.slot(0);
        Expression right = match.slot(1);
        if (left.isList()) {
            return Optional.empty();
        }
        SkType rightType = right instanceof ListExpression list ? list.type() : right.type();
        if (!Comparators.canCompare(left.type(), rightType)) {
            Optional<Expression> other = comparableAlternative(left.type(), right);
            if (other.isEmpty()) {
                return Optional.empty();
            }
            right = other.get();
            rightType = right instanceof ListExpression list ? list.type() : right.type();
        }
        if (relation.ordered() && !Comparators.canOrder(left.type(), rightType)) {
            return Optional.empty();
        }
        boolean disjunctive = right instanceof ListExpression list && list.disjunctive();
        return Optional.of(new Comparison(left, right, null, relation, negate, disjunctive));
    }

    private static Optional<Expression> comparableAlternative(SkType leftType, Expression right) {
        Optional<Expression> other = AmbiguousExpression.alternative(right);
        if (other.isPresent() && Comparators.canCompare(leftType, other.get().type())) {
            return other;
        }
        SkType target = leftType == SkType.ENTITY ? SkType.ENTITYTYPE : leftType;
        return Literals.reinterpret(right, target);
    }

    static Optional<Condition> createBetween(Match match, boolean negate) {
        Expression left = match.slot(0);
        Expression low = match.slot(1);
        Expression high = match.slot(2);
        if (left.isList() || low.isList() || high.isList()) {
            return Optional.empty();
        }
        if (!Comparators.canOrder(left.type(), low.type()) || !Comparators.canOrder(left.type(), high.type())) {
            return Optional.empty();
        }
        return Optional.of(new Comparison(left, low, high, Relation.EQUAL, negate, false));
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
