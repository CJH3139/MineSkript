package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

public final class AmbiguousExpression implements Expression {
    private final Expression primary;
    private final Expression alternative;

    private AmbiguousExpression(Expression primary, Expression alternative) {
        this.primary = primary;
        this.alternative = alternative;
    }

    public static Expression of(Expression primary, List<Token> tokens) {
        if (primary instanceof AmbiguousExpression || primary.isList() || primary.type() == SkType.BLOCKTYPE) {
            return primary;
        }
        return Literals.blockType(tokens)
                .<Expression>map(type -> new AmbiguousExpression(primary, new ConstantExpression(SkType.BLOCKTYPE, type)))
                .orElse(primary);
    }

    public static Optional<Expression> alternative(Expression expression) {
        return expression instanceof AmbiguousExpression ambiguous ? Optional.of(ambiguous.alternative) : Optional.empty();
    }

    @Override
    public SkType type() {
        return primary.type();
    }

    @Override
    public boolean isList() {
        return primary.isList();
    }

    @Override
    public Optional<Changeable> changer() {
        return primary.changer();
    }

    @Override
    public Object evaluate(Context context) {
        return primary.evaluate(context);
    }
}
