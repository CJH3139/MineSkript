package com.mineskript.syntax.expressions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;
import java.util.function.Function;

public final class ExprHeldItem implements Expression {
    private final Function<GameBridge, Object> reader;

    private ExprHeldItem(Function<GameBridge, Object> reader) {
        this.reader = reader;
    }

    public static void register(SyntaxRegistry registry) {
        simple(registry, GameBridge::heldItem, "[the] (held item|item in hand|tool)");
        simple(registry, GameBridge::offhandItem, "[the] (offhand item|item in offhand)");
    }

    private static void simple(SyntaxRegistry registry, Function<GameBridge, Object> reader, String pattern) {
        registry.addExpression(SkType.ITEM, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprHeldItem(reader)), pattern);
    }

    @Override
    public SkType type() {
        return SkType.ITEM;
    }

    @Override
    public Object evaluate(Context context) {
        return reader.apply(context.world());
    }
}
