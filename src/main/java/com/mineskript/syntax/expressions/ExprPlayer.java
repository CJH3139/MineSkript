package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprPlayer implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.PLAYER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprPlayer()), "[the] player");
    }

    @Override
    public SkType type() {
        return SkType.PLAYER;
    }

    @Override
    public Object evaluate(Context context) {
        return PlayerRef.LOCAL;
    }
}
