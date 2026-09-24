package com.mineskript.common.elements.expressions;

import com.mineskript.lang.ast.None;

/** Shared number parsing for the parsed as expressions. Not a syntax element. */
final class ParsedNumbers {
    private ParsedNumbers() {
    }

    static Object number(String value) {
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : None.NONE;
        } catch (NumberFormatException error) {
            return None.NONE;
        }
    }
}
