package com.mineskript.common.elements.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

final class TextHelper {
    private TextHelper() {
    }

    static int index(Expression expression, Context context) {
        double value = (Double) expression.evaluate(context);
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.round(value);
    }

    static String range(String text, int from, int to) {
        int start = Math.max(1, from) - 1;
        int end = Math.min(text.length(), to);
        if (start >= text.length() || end <= start) {
            return "";
        }
        return text.substring(start, end);
    }

    static String last(String text, int count) {
        int take = Math.max(0, Math.min(text.length(), count));
        return text.substring(text.length() - take);
    }

    static List<String> texts(Object value, Context context) {
        List<String> texts = new ArrayList<>();
        if (value instanceof List<?> items) {
            for (Object item : items) {
                if (item != None.NONE) {
                    texts.add(Converters.toText(item, context));
                }
            }
        } else if (value != None.NONE) {
            texts.add(Converters.toText(value, context));
        }
        return texts;
    }

    static Object map(Expression text, Context context, UnaryOperator<String> change) {
        if (text.isList()) {
            return texts(text.evaluate(context), context).stream().map(change).toList();
        }
        return change.apply(Converters.toText(text.evaluate(context), context));
    }
}
