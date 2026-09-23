package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class Arithmetic {
    private static final Set<String> ADDITIVE = Set.of("+", "-");
    private static final Set<String> MULTIPLICATIVE = Set.of("*", "/");

    private Arithmetic() {
    }

    public static Optional<Expression> parse(List<Token> tokens, ExpressionParser parser, List<SkType> types, ParseScope scope) {
        if (wrapped(tokens)) {
            return parser.parse(tokens.subList(1, tokens.size() - 1), types, scope);
        }
        int split = splitPoint(tokens);
        if (split < 0) {
            return Optional.empty();
        }
        Optional<Expression> left = parser.parse(tokens.subList(0, split), List.of(SkType.OBJECT), scope);
        Optional<Expression> right = parser.parse(tokens.subList(split + 1, tokens.size()), List.of(SkType.OBJECT), scope);
        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ArithmeticExpression(tokens.get(split).text().charAt(0), left.get(), right.get()));
    }

    public static boolean wrapped(List<Token> tokens) {
        if (tokens.size() < 3 || !tokens.get(0).is("(") || !tokens.get(tokens.size() - 1).is(")")) {
            return false;
        }
        int depth = 0;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).is("(")) {
                depth++;
            } else if (tokens.get(i).is(")")) {
                depth--;
                if (depth == 0 && i < tokens.size() - 1) {
                    return false;
                }
            }
        }
        return depth == 0;
    }

    private static int splitPoint(List<Token> tokens) {
        int additive = -1;
        int multiplicative = -1;
        int power = -1;
        int depth = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.is("(")) {
                depth++;
                continue;
            }
            if (token.is(")")) {
                depth--;
                continue;
            }
            if (depth != 0 || token.quoted() || i == 0 || i == tokens.size() - 1) {
                continue;
            }
            if (ADDITIVE.contains(token.text())) {
                additive = i;
            } else if (MULTIPLICATIVE.contains(token.text())) {
                multiplicative = i;
            } else if (token.text().equals("^") && power < 0) {
                power = i;
            }
        }
        if (additive >= 0) {
            return additive;
        }
        if (multiplicative >= 0) {
            return multiplicative;
        }
        return power;
    }
}
