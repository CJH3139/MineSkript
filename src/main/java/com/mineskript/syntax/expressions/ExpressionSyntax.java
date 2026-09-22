package com.mineskript.syntax.expressions;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class ExpressionSyntax {
    private ExpressionSyntax() {
    }

    public static void register(SyntaxRegistry registry) {
        ExprPlayer.register(registry);
        ExprMessage.register(registry);
        ExprPlayerProperty.register(registry);
        ExprCoordinate.register(registry);
        ExprBlock.register(registry);
    }
}
