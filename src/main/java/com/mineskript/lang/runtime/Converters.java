package com.mineskript.lang.runtime;

import com.mineskript.lang.Language;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public final class Converters {
    public static final int DISPLAY_DECIMALS = 2;

    private Converters() {
    }

    public static String toText(Object value, Context context) {
        return switch (value) {
            case null -> "<none>";
            case None ignored -> "<none>";
            case String text -> text;
            case Double number -> formatNumber(number);
            case Boolean bool -> bool.toString();
            case Timespan timespan -> timespan.toString();
            case BlockType type -> type.path();
            case BlockValue block -> block.type().path();
            case PlayerRef ignored -> context.world().playerName();
            case ItemValue item -> item.count() > 1 ? item.count() + " " + item.name() : item.name();
            case EntityValue entity -> entity.name();
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
            case ItemValue ignored -> SkType.ITEM;
            case EntityValue ignored -> SkType.ENTITY;
            case null, default -> SkType.OBJECT;
        };
    }

    public static boolean canConvert(SkType from, SkType to) {
        if (to == SkType.OBJECT || to == from || to == SkType.TEXT) {
            return true;
        }
        if (from == SkType.BLOCK && to == SkType.BLOCKTYPE) {
            return true;
        }
        return from == SkType.ITEM && to == SkType.BLOCKTYPE;
    }

    public static Object convert(Object value, SkType to, Context context) {
        if (value instanceof List<?> list) {
            return list.stream().map(item -> convert(item, to, context)).toList();
        }
        if (value == None.NONE) {
            if (to == SkType.OBJECT) {
                return value;
            }
            if (to == SkType.TEXT) {
                return "<none>";
            }
            throw new ScriptError(Language.get("runtime.variable-not-set"));
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
        if (to == SkType.BLOCKTYPE && value instanceof ItemValue item) {
            return item.type();
        }
        throw new ScriptError(Language.format("runtime.cannot-convert", typeName(typeOf(value)), typeName(to)));
    }

    public static String typeName(SkType type) {
        return type.name().toLowerCase(Locale.ROOT).replace("blocktype", "block type");
    }

    private static String formatNumber(double number) {
        if (!Double.isFinite(number)) {
            return Double.toString(number);
        }
        if (number == Math.rint(number) && Math.abs(number) < 1.0e15) {
            return Long.toString((long) number);
        }
        BigDecimal rounded = BigDecimal.valueOf(number).setScale(DISPLAY_DECIMALS, RoundingMode.HALF_UP).stripTrailingZeros();
        return rounded.signum() == 0 ? "0" : rounded.toPlainString();
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
