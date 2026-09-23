package com.mineskript.syntax.expressions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;
import java.util.function.Function;

public final class ExprWorld implements Expression {
    private final SkType type;
    private final Function<GameBridge, Object> reader;

    private ExprWorld(SkType type, Function<GameBridge, Object> reader) {
        this.type = type;
        this.reader = reader;
    }

    public static void register(SyntaxRegistry registry) {
        simple(registry, SkType.NUMBER, game -> (double) game.gameTime(), "[the] game time");
        simple(registry, SkType.NUMBER, game -> (double) game.playersOnline(), "[the] players online");
        simple(registry, SkType.TEXT, GameBridge::difficulty, "[the] difficulty");
    }

    private static void simple(SyntaxRegistry registry, SkType type, Function<GameBridge, Object> reader, String pattern) {
        registry.addExpression(type, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprWorld(type, reader)), pattern);
    }

    @Override
    public SkType type() {
        return type;
    }

    @Override
    public Object evaluate(Context context) {
        return reader.apply(context.world());
    }
}
