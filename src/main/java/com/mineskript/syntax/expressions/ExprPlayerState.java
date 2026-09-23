package com.mineskript.syntax.expressions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;
import java.util.function.Function;

public final class ExprPlayerState implements Expression {
    private final SkType type;
    private final Function<GameBridge, Object> reader;

    private ExprPlayerState(SkType type, Function<GameBridge, Object> reader) {
        this.type = type;
        this.reader = reader;
    }

    public static void register(SyntaxRegistry registry) {
        simple(registry, SkType.TEXT, GameBridge::gamemode, "[the] gamemode");
        simple(registry, SkType.NUMBER, game -> (double) game.xpLevel(), "[the] (xp level|experience level)");
        simple(registry, SkType.NUMBER, GameBridge::xpProgress, "[the] (xp progress|experience progress)");
        simple(registry, SkType.NUMBER, game -> (double) game.air(), "[the] air");
        simple(registry, SkType.NUMBER, game -> (double) game.maxAir(), "[the] max air");
        simple(registry, SkType.NUMBER, game -> (double) game.armor(), "[the] armor");
        simple(registry, SkType.NUMBER, GameBridge::yaw, "[the] yaw");
        simple(registry, SkType.NUMBER, GameBridge::pitch, "[the] pitch");
        simple(registry, SkType.NUMBER, GameBridge::speed, "[the] speed");
        simple(registry, SkType.NUMBER, GameBridge::fallDistance, "[the] fall distance");
        simple(registry, SkType.TEXT, GameBridge::dimension, "[the] dimension");
        simple(registry, SkType.NUMBER, game -> (double) game.selectedSlot(), "[the] selected slot");
        simple(registry, SkType.NUMBER, game -> (double) game.freeSlots(), "[the] free slots");
        simple(registry, SkType.NUMBER, game -> (double) (game.inventorySize() - game.freeSlots()), "[the] used slots");
    }

    private static void simple(SyntaxRegistry registry, SkType type, Function<GameBridge, Object> reader, String pattern) {
        registry.addExpression(type, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprPlayerState(type, reader)), pattern);
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
