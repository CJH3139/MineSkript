package com.mineskript.lang.lexer;

import java.util.Optional;

/**
 * Finds where strings, %...% interpolations and {variables} end, so the lexer's comment stripping, the tokenizer and
 * text literals all agree on them. Inside a string "" is one literal quote and %% one literal percent sign; inside an
 * interpolation or a variable name, strings and variables nest.
 */
public final class TextScanner {
    /** The text between a string's quotes and the index of its closing quote. */
    public record Quoted(String content, int end) {
    }

    private TextScanner() {
    }

    /**
     * Reads the string whose opening quote is at {@code open}. Outside interpolations "" becomes one quote; the text of
     * an interpolation is kept exactly as written so it can be tokenized again. Empty when the string never ends.
     */
    public static Optional<Quoted> readString(String text, int open) {
        StringBuilder content = new StringBuilder();
        int i = open + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '"') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    content.append('"');
                    i += 2;
                    continue;
                }
                return Optional.of(new Quoted(content.toString(), i));
            }
            if (c == '%') {
                int close = i + 1 < text.length() && text.charAt(i + 1) == '%' ? i + 1 : closingPercent(text, i);
                int end = close < 0 ? i : close;
                content.append(text, i, end + 1);
                i = end + 1;
                continue;
            }
            content.append(c);
            i++;
        }
        return Optional.empty();
    }

    /** The index of the quote closing the string opened at {@code open}, or -1 when it never ends. */
    public static int closingQuote(String text, int open) {
        return readString(text, open).map(Quoted::end).orElse(-1);
    }

    /** The index of the % closing the interpolation opened at {@code open}, or -1 when there is none. */
    public static int closingPercent(String text, int open) {
        int i = open + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '%') {
                return i;
            }
            int end = c == '"' ? closingQuote(text, i) : c == '{' ? closingBrace(text, i) : i;
            if (end < 0) {
                return -1;
            }
            i = end + 1;
        }
        return -1;
    }

    /** The index of the } closing the variable opened at {@code open}, or -1 when it never ends. */
    public static int closingBrace(String text, int open) {
        int i = open + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '}') {
                return i;
            }
            if (c == '{') {
                int end = closingBrace(text, i);
                if (end < 0) {
                    return -1;
                }
                i = end + 1;
            } else if (c == '%') {
                int end = closingPercent(text, i);
                i = end < 0 ? i + 1 : end + 1;
            } else {
                i++;
            }
        }
        return -1;
    }
}
