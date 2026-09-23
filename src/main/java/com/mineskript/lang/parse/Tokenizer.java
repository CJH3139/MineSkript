package com.mineskript.lang.parse;

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
                int end = line.indexOf('"', i + 1);
                if (end < 0) {
                    throw new TokenizeException("unterminated string");
                }
                tokens.add(new Token(line.substring(i + 1, end), true));
                i = end + 1;
            } else if (c == '{') {
                flush(tokens, word);
                int end = line.indexOf('}', i + 1);
                if (end < 0) {
                    throw new TokenizeException("unterminated variable");
                }
                String inner = line.substring(i + 1, end).trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
                tokens.add(new Token("{" + inner + "}", false));
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
