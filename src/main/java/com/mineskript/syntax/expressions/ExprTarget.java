package com.mineskript.syntax.expressions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;
import java.util.function.Function;

public final class ExprTarget implements Expression {
    private final SkType type;
    private final Function<GameBridge, Object> reader;

    private ExprTarget(SkType type, Function<GameBridge, Object> reader) {
        this.type = type;
        this.reader = reader;
    }

    public static void register(SyntaxRegistry registry) {
        simple(registry, SkType.BLOCK, game -> new BlockValue(game.targetBlock()), "[the] target block");
        simple(registry, SkType.ENTITY, game -> nullToNone(game.targetEntity()), "[the] target entity");
    }

    private static void simple(SyntaxRegistry registry, SkType type, Function<GameBridge, Object> reader, String pattern) {
        registry.addExpression(type, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprTarget(type, reader)), pattern);
    }

    private static Object nullToNone(Object value) {
        return value == null ? None.NONE : value;
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
