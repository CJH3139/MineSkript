package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;
import java.util.List;
import java.util.Locale;

public final class Converters {
    private Converters() {
    }

    public static String toText(Object value, Context context) {
        return switch (value) {
            case null -> "<none>";
            case String text -> text;
            case Double number -> formatNumber(number);
            case Boolean bool -> bool.toString();
            case Timespan timespan -> timespan.toString();
            case BlockType type -> type.path();
            case BlockValue block -> block.type().path();
            case PlayerRef ignored -> context.world().playerName();
            case List<?> list -> joinList(list, context);
            default -> value.toString();
        };
    }

    public static SkType typeOf(Object value) {
        return switch (value) {
            case String ignored -> SkType.TEXT;
            case Double ignored -> SkType.NUMBER;
            case Boolean ignored -> SkType.BOOLEAN;
            case Timespan ignored -> SkType.TIMESPAN;
            case BlockType ignored -> SkType.BLOCKTYPE;
            case BlockValue ignored -> SkType.BLOCK;
            case PlayerRef ignored -> SkType.PLAYER;
            case null, default -> SkType.OBJECT;
        };
    }

    public static boolean canConvert(SkType from, SkType to) {
        if (to == SkType.OBJECT || to == from || to == SkType.TEXT) {
            return true;
        }
        return from == SkType.BLOCK && to == SkType.BLOCKTYPE;
    }

    public static Object convert(Object value, SkType to, Context context) {
        if (value instanceof List<?> list) {
            return list.stream().map(item -> convert(item, to, context)).toList();
        }
        if (to == SkType.OBJECT || typeOf(value) == to) {
            return value;
        }
        if (to == SkType.TEXT) {
            return toText(value, context);
        }
        if (to == SkType.BLOCKTYPE && value instanceof BlockValue block) {
            return block.type();
        }
        throw new ScriptError("cannot convert " + typeName(typeOf(value)) + " to " + typeName(to));
    }

    public static String typeName(SkType type) {
        return type.name().toLowerCase(Locale.ROOT).replace("blocktype", "block type");
    }

    private static String formatNumber(double number) {
        if (number == Math.rint(number) && Math.abs(number) < 1.0e15) {
            return Long.toString((long) number);
        }
        return Double.toString(number);
    }

    private static String joinList(List<?> list, Context context) {
        if (list.isEmpty()) {
            return "";
        }
        if (list.size() == 1) {
            return toText(list.get(0), context);
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                text.append(i == list.size() - 1 ? " and " : ", ");
            }
            text.append(toText(list.get(i), context));
        }
        return text.toString();
    }
}
