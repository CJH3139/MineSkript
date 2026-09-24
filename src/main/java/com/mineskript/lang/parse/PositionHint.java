package com.mineskript.lang.parse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

final class PositionHint {
    private static final Set<String> LEADS = Set.of("at", "of", "to");

    private PositionHint() {
    }

    static Optional<List<String>> find(List<Token> tokens, Predicate<List<Token>> isNumber) {
        for (int lead = 0; lead < tokens.size(); lead++) {
            if (!tokens.get(lead).quoted() && LEADS.contains(tokens.get(lead).text())) {
                Optional<List<String>> found = numbersAfter(tokens, lead + 1, isNumber);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<List<String>> numbersAfter(List<Token> tokens, int start, Predicate<List<Token>> isNumber) {
        int firstComma = nextComma(tokens, start);
        int secondComma = firstComma < 0 ? -1 : nextComma(tokens, firstComma + 1);
        if (secondComma < 0) {
            return Optional.empty();
        }
        List<Token> x = tokens.subList(start, firstComma);
        List<Token> y = tokens.subList(firstComma + 1, secondComma);
        if (x.isEmpty() || y.isEmpty() || !isNumber.test(x) || !isNumber.test(y)) {
            return Optional.empty();
        }
        int end = nextComma(tokens, secondComma + 1);
        int limit = end < 0 ? tokens.size() : end;
        for (int last = secondComma + 2; last <= limit; last++) {
            List<Token> z = tokens.subList(secondComma + 1, last);
            if (isNumber.test(z)) {
                return Optional.of(List.of(text(x), text(y), text(z)));
            }
        }
        return Optional.empty();
    }

    private static int nextComma(List<Token> tokens, int from) {
        int depth = 0;
        for (int i = from; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.is("(")) {
                depth++;
            } else if (token.is(")")) {
                depth--;
            } else if (depth == 0 && token.is(",")) {
                return i;
            }
        }
        return -1;
    }

    private static String text(List<Token> tokens) {
        StringBuilder text = new StringBuilder();
        String previous = "";
        for (Token token : tokens) {
            String word = token.quoted() ? "\"" + token.text() + "\"" : token.text();
            boolean attached = !token.quoted() && (word.equals(",") || word.equals(")") || word.equals("'s"));
            if (!text.isEmpty() && !attached && !previous.equals("(")) {
                text.append(' ');
            }
            text.append(word);
            previous = token.quoted() ? "" : word;
        }
        return text.toString();
    }
}
