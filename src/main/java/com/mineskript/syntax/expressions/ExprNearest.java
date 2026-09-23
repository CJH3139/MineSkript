package com.mineskript.syntax.expressions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;
import java.util.function.Function;

public final class ExprNearest implements Expression {
    private final Function<GameBridge, Object> reader;

    private ExprNearest(Function<GameBridge, Object> reader) {
        this.reader = reader;
    }

    public static void register(SyntaxRegistry registry) {
        simple(registry, game -> nullToNone(game.nearestEntity()), "[the] nearest entity");
        simple(registry, game -> nullToNone(game.nearestPlayer()), "[the] nearest player");
    }

    private static void simple(SyntaxRegistry registry, Function<GameBridge, Object> reader, String pattern) {
        registry.addExpression(SkType.ENTITY, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprNearest(reader)), pattern);
    }

    private static Object nullToNone(Object value) {
        return value == null ? None.NONE : value;
    }

    @Override
    public SkType type() {
        return SkType.ENTITY;
    }

    @Override
    public Object evaluate(Context context) {
        return reader.apply(context.world());
    }
}
