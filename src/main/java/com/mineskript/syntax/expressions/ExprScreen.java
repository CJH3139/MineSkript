package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class ExprScreen implements Expression {
    private final boolean title;

    private ExprScreen(boolean title) {
        this.title = title;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprScreen(true)),
                "[the] [(current|open)] screen title", "[the] title of [the] [(current|open)] screen");
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprScreen(false)),
                "[the] [(current|open)] screen (type|kind)", "[the] (type|kind) of [the] [(current|open)] screen");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return title ? context.world().screenTitle() : context.world().screenType();
    }
}
