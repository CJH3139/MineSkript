package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;

public final class Comparators {
    private Comparators() {
    }

    public static boolean canCompare(SkType a, SkType b) {
        if (a == SkType.OBJECT || b == SkType.OBJECT) {
            return true;
        }
        if (a == b) {
            return a != SkType.PLAYER;
        }
        return isBlockish(a) && isBlockish(b);
    }

    public static boolean canOrder(SkType a, SkType b) {
        if (a == SkType.OBJECT || b == SkType.OBJECT) {
            return true;
        }
        return a == b && (a == SkType.NUMBER || a == SkType.TIMESPAN);
    }

    public static int relate(Object a, Object b) {
        Object left = normalise(a);
        Object right = normalise(b);
        if (left instanceof Double x && right instanceof Double y) {
            return Double.compare(x, y);
        }
        if (left instanceof Timespan x && right instanceof Timespan y) {
            return Integer.compare(x.ticks(), y.ticks());
        }
        if (left instanceof String x && right instanceof String y) {
            return x.equals(y) ? 0 : 1;
        }
        if (left instanceof BlockType x && right instanceof BlockType y) {
            return x.id().equals(y.id()) ? 0 : 1;
        }
        if (left instanceof Boolean x && right instanceof Boolean y) {
            return x.equals(y) ? 0 : 1;
        }
        throw new ScriptError("cannot compare " + Converters.typeName(Converters.typeOf(a)) + " with " + Converters.typeName(Converters.typeOf(b)));
    }

    private static boolean isBlockish(SkType type) {
        return type == SkType.BLOCK || type == SkType.BLOCKTYPE;
    }

    private static Object normalise(Object value) {
        return value instanceof BlockValue block ? block.type() : value;
    }
}
