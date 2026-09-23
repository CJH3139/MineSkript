package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Converters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ExpressionParser implements SlotResolver {
    private final SyntaxRegistry registry;
    private final Set<String> inProgress = new HashSet<>();
    private final Map<String, Optional<Expression>> cache = new HashMap<>();
    private boolean blocked;
    private Parser owner;

    public ExpressionParser(SyntaxRegistry registry) {
        this.registry = registry;
    }

    void attach(Parser parser) {
        owner = parser;
    }

    public void clearCache() {
        cache.clear();
    }

    public Optional<Expression> parse(String text, SkType type, ParseScope scope) {
        return parse(Tokenizer.tokenize(text), List.of(type), scope);
    }

    @Override
    public Optional<Expression> resolve(List<Token> tokens, List<SkType> types, ParseScope scope) {
        return parse(tokens, types, scope);
    }

    public Optional<Expression> parse(List<Token> tokens, List<SkType> types, ParseScope scope) {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        String key = tokens + "|" + types + "|" + scope.event() + "|" + scope.loopDepth() + "|" + scope.loop();
        Optional<Expression> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        boolean outerBlocked = blocked;
        blocked = false;
        try {
            Optional<Expression> result = parseUncached(tokens, types, scope);
            if (!blocked) {
                cache.put(key, result);
            }
            return result;
        } finally {
            blocked = outerBlocked || blocked;
        }
    }

    private Optional<Expression> parseUncached(List<Token> tokens, List<SkType> types, ParseScope scope) {
        if (owner != null) {
            Optional<Expression> call = owner.functionCall(tokens, scope).flatMap(expression -> typed(expression, types));
            if (call.isPresent()) {
                return call;
            }
        }
        Optional<Expression> literal = literal(tokens, types, scope);
        if (literal.isPresent()) {
            return literal;
        }
        if (owner != null) {
            Optional<Expression> ternary = owner.ternary(tokens, types, scope).flatMap(expression -> typed(expression, types));
            if (ternary.isPresent()) {
                return ternary;
            }
        }
        Optional<Expression> list = list(tokens, types, scope);
        if (list.isPresent()) {
            return list;
        }
        Optional<Expression> arithmetic = Arithmetic.parse(tokens, this, types, scope).flatMap(expression -> typed(expression, types));
        if (arithmetic.isPresent()) {
            return arithmetic;
        }
        for (Tier tier : Tier.values()) {
            for (ExpressionEntry entry : registry.expressions(tier)) {
                if (!accepted(entry.returnType(), types)) {
                    continue;
                }
                String key = System.identityHashCode(entry) + "|" + tokens;
                if (!inProgress.add(key)) {
                    blocked = true;
                    continue;
                }
                try {
                    Optional<Expression> matched = registry.matchEntry(entry, tokens, this, scope);
                    if (matched.isPresent()) {
                        Optional<Expression> typed = typed(matched.get(), types);
                        if (typed.isPresent()) {
                            return expectsBlockType(types)
                                    ? Optional.of(AmbiguousExpression.of(typed.get(), tokens))
                                    : typed;
                        }
                    }
                } finally {
                    inProgress.remove(key);
                }
            }
        }
        if (accepted(SkType.BLOCKTYPE, types)) {
            return Literals.blockType(tokens).flatMap(type -> typed(new ConstantExpression(SkType.BLOCKTYPE, type), types));
        }
        return Optional.empty();
    }

    private Optional<Expression> literal(List<Token> tokens, List<SkType> types, ParseScope scope) {
        if (tokens.size() == 1 && !tokens.get(0).quoted() && tokens.get(0).text().startsWith("{")) {
            return typed(VariableExpression.of(tokens.get(0).text()), types);
        }
        if (tokens.size() == 1 && !tokens.get(0).quoted()) {
            switch (tokens.get(0).text()) {
                case "true", "yes" -> {
                    return typed(new ConstantExpression(SkType.BOOLEAN, Boolean.TRUE), types);
                }
                case "false", "no" -> {
                    return typed(new ConstantExpression(SkType.BOOLEAN, Boolean.FALSE), types);
                }
                default -> {
                }
            }
        }
        if (tokens.size() == 1 && tokens.get(0).quoted()) {
            return TextLiteral.parse(tokens.get(0).text(), this, scope).flatMap(expression -> typed(expression, types));
        }
        Optional<Double> number = Literals.number(tokens);
        if (number.isPresent()) {
            return typed(new ConstantExpression(SkType.NUMBER, number.get()), types);
        }
        return Literals.timespan(tokens).flatMap(timespan -> typed(new ConstantExpression(SkType.TIMESPAN, timespan), types));
    }

    private Optional<Expression> list(List<Token> tokens, List<SkType> types, ParseScope scope) {
        Optional<Literals.Split> split = Literals.splitList(tokens);
        if (split.isEmpty()) {
            return Optional.empty();
        }
        List<Expression> items = new ArrayList<>();
        for (List<Token> part : split.get().parts()) {
            Optional<Expression> item = parse(part, types, scope);
            if (item.isEmpty() || item.get().isList()) {
                return Optional.empty();
            }
            items.add(item.get());
        }
        SkType type = items.get(0).type();
        if (items.stream().anyMatch(item -> item.type() != type)) {
            return Optional.empty();
        }
        return Optional.of(new ListExpression(items, type, split.get().disjunctive()));
    }

    private static Optional<Expression> typed(Expression expression, List<SkType> types) {
        if (expression.type() == SkType.OBJECT && !expression.isList()) {
            SkType target = types.get(0);
            return Optional.of(target == SkType.OBJECT ? expression : new ConvertedExpression(expression, target));
        }
        for (SkType type : types) {
            if (type.accepts(expression.type())) {
                return Optional.of(expression);
            }
        }
        for (SkType type : types) {
            if (Converters.canConvert(expression.type(), type)) {
                return Optional.of(new ConvertedExpression(expression, type));
            }
        }
        return Optional.empty();
    }

    private static boolean expectsBlockType(List<SkType> types) {
        return types.stream().anyMatch(type -> type == SkType.OBJECT || type == SkType.BLOCKTYPE || type == SkType.BLOCK || type == SkType.ITEM);
    }

    private static boolean accepted(SkType returnType, List<SkType> types) {
        return returnType == SkType.OBJECT || types.stream().anyMatch(type -> type.accepts(returnType) || Converters.canConvert(returnType, type));
    }
}
