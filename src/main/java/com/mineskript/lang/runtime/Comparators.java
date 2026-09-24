package com.mineskript.lang.runtime;

import com.mineskript.lang.Language;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.None;
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
        if (isEntityText(a, b) || isEntityText(b, a)) {
            return true;
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
        if (left instanceof EntityValue x && right instanceof EntityValue y) {
            return x.id().equals(y.id()) ? 0 : 1;
        }
        if (left instanceof EntityValue x && right instanceof String y) {
            return x.id().equals(BlockType.fromWords(y).id()) ? 0 : 1;
        }
        if (left instanceof String x && right instanceof EntityValue y) {
            return y.id().equals(BlockType.fromWords(x).id()) ? 0 : 1;
        }
        throw new ScriptError(Language.format("runtime.cannot-compare",
                Converters.typeName(Converters.typeOf(a)), Converters.typeName(Converters.typeOf(b))));
    }

    public static boolean test(Relation relation, Object a, Object b) {
        if (a == None.NONE || b == None.NONE) {
            return relation == Relation.NOT_EQUAL;
        }
        return relation.holds(relate(a, b));
    }

    private static boolean isEntityText(SkType a, SkType b) {
        return a == SkType.ENTITY && b == SkType.TEXT;
    }

    private static boolean isBlockish(SkType type) {
        return type == SkType.BLOCK || type == SkType.BLOCKTYPE || type == SkType.ITEM;
    }

    private static Object normalise(Object value) {
        if (value instanceof BlockValue block) {
            return block.type();
        }
        if (value instanceof ItemValue item) {
            return item.type();
        }
        return value;
    }
}
