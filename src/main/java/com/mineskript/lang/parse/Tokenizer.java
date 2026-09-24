package com.mineskript.lang.parse;

import com.mineskript.lang.Language;
import com.mineskript.lang.lexer.TextScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Tokenizer {
    private Tokenizer() {
    }

    public static List<Token> tokenize(String line) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '"') {
                flush(tokens, word);
                TextScanner.Quoted quoted = TextScanner.readString(line, i)
                        .orElseThrow(() -> new TokenizeException(Language.get("tokenizer.unterminated-string")));
                tokens.add(new Token(quoted.content(), true));
                i = quoted.end() + 1;
            } else if (c == '{') {
                flush(tokens, word);
                int end = TextScanner.closingBrace(line, i);
                if (end < 0) {
                    throw new TokenizeException(Language.get("tokenizer.unterminated-variable"));
                }
                tokens.add(new Token("{" + variableName(line.substring(i + 1, end)) + "}", false));
                i = end + 1;
            } else if (Character.isWhitespace(c)) {
                flush(tokens, word);
                i++;
            } else if (c == ',' || c == '(' || c == ')') {
                flush(tokens, word);
                tokens.add(new Token(String.valueOf(c), false));
                i++;
            } else {
                word.append(c);
                i++;
            }
        }
        flush(tokens, word);
        return tokens;
    }

    /**
     * Lowercases a variable name and collapses its whitespace, leaving each %...% part exactly as written because it is
     * an expression that is parsed on its own.
     */
    private static String variableName(String raw) {
        StringBuilder name = new StringBuilder();
        StringBuilder plain = new StringBuilder();
        int i = 0;
        while (i < raw.length()) {
            int end = raw.charAt(i) == '%' ? TextScanner.closingPercent(raw, i) : -1;
            if (end < 0) {
                plain.append(raw.charAt(i));
                i++;
                continue;
            }
            name.append(normalise(plain));
            name.append(raw, i, end + 1);
            i = end + 1;
        }
        name.append(normalise(plain));
        return name.toString().strip();
    }

    private static String normalise(StringBuilder plain) {
        String text = plain.toString().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        plain.setLength(0);
        return text;
    }

    private static void flush(List<Token> tokens, StringBuilder word) {
        if (word.isEmpty()) {
            return;
        }
        String text = word.toString().toLowerCase(Locale.ROOT);
        word.setLength(0);
        if (text.length() > 2 && text.endsWith("'s")) {
            tokens.add(new Token(text.substring(0, text.length() - 2), false));
            tokens.add(new Token("'s", false));
        } else {
            tokens.add(new Token(text, false));
        }
    }
}
