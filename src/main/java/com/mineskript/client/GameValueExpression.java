package com.mineskript.client;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import java.util.function.Function;

public abstract class GameValueExpression implements Expression {
    private final SkType type;
    private final Function<GameBridge, Object> reader;

    protected GameValueExpression(SkType type, Function<GameBridge, Object> reader) {
        this.type = type;
        this.reader = reader;
    }

    protected static double changed(ChangeMode mode, double current, Object value) {
        double amount = (Double) value;
        return switch (mode) {
            case ADD -> current + amount;
            case REMOVE -> current - amount;
            default -> amount;
        };
    }

    protected static Object nullToNone(Object value) {
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
