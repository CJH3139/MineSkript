package com.mineskript.common.elements.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.runtime.Context;

/** Shared text helpers for the character range expressions. Not a syntax element. */
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
}
