package com.mineskript.syntax.expressions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;
import java.util.function.Function;

public final class ExprPlayerProperty implements Expression {
    private final SkType type;
    private final Function<GameBridge, Object> reader;

    private ExprPlayerProperty(SkType type, Function<GameBridge, Object> reader) {
        this.type = type;
        this.reader = reader;
    }

    public static void register(SyntaxRegistry registry) {
        property(registry, "health", SkType.NUMBER, game -> game.playerHealth());
        property(registry, "max health", SkType.NUMBER, game -> game.playerMaxHealth());
        property(registry, "(hunger|food level)", SkType.NUMBER, game -> (double) game.playerHunger());
        property(registry, "name", SkType.TEXT, GameBridge::playerName);
    }

    private static void property(SyntaxRegistry registry, String words, SkType type, Function<GameBridge, Object> reader) {
        registry.addExpression(type, Tier.PROPERTY, (match, scope) -> Optional.of(new ExprPlayerProperty(type, reader)),
                "[the] " + words + " of %player%",
                "%player%'s " + words);
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
