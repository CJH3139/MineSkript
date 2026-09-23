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

public final class ExprClient implements Expression {
    private final SkType type;
    private final boolean list;
    private final boolean needsWorld;
    private final Function<GameBridge, Object> reader;

    private ExprClient(SkType type, boolean list, boolean needsWorld, Function<GameBridge, Object> reader) {
        this.type = type;
        this.list = list;
        this.needsWorld = needsWorld;
        this.reader = reader;
    }

    public static void register(SyntaxRegistry registry) {
        simple(registry, SkType.TEXT, false, GameBridge::biome, "[the] biome");
        simple(registry, SkType.NUMBER, false, game -> (double) game.lightLevel(), "[the] light level");
        simple(registry, SkType.NUMBER, false, game -> (double) game.skyLight(), "[the] (sky light|sky light level)");
        anywhere(registry, SkType.TEXT, GameBridge::serverAddress, "[the] server (address|ip)");
        anywhere(registry, SkType.TEXT, GameBridge::serverBrand, "[the] server brand");
        anywhere(registry, SkType.NUMBER, game -> (double) game.ping(), "[the] ping");
        anywhere(registry, SkType.NUMBER, game -> (double) game.fps(), "[the] (fps|frame rate)");
        simple(registry, SkType.NUMBER, false, GameBridge::saturation, "[the] saturation");
        simple(registry, SkType.NUMBER, false, game -> (double) game.totalExperience(), "[the] (total xp|total experience)");
        simple(registry, SkType.ENTITY, false, game -> nullToNone(game.vehicle()), "[the] vehicle");
        simple(registry, SkType.TEXT, true, GameBridge::onlinePlayerNames, "[the] online player (name|names)");
    }

    private static void simple(SyntaxRegistry registry, SkType type, boolean list, Function<GameBridge, Object> reader, String pattern) {
        registry.addExpression(type, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprClient(type, list, true, reader)), pattern);
    }

    private static void anywhere(SyntaxRegistry registry, SkType type, Function<GameBridge, Object> reader, String pattern) {
        registry.addExpression(type, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprClient(type, false, false, reader)), pattern);
    }

    private static Object nullToNone(Object value) {
        return value == null ? None.NONE : value;
    }

    @Override
    public SkType type() {
        return type;
    }

    @Override
    public boolean isList() {
        return list;
    }

    @Override
    public Object evaluate(Context context) {
        return reader.apply(needsWorld ? context.world() : context.game());
    }
}
