package com.mineskript.lang.parse;

import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.NamedValues;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class Literals {
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern WORD = Pattern.compile("[a-z_][a-z0-9_]*(:[a-z0-9_/.]+)?");
    private static final Set<String> RESERVED = Set.of(
            "is", "are", "not", "or", "and", "of", "the", "player", "message", "key",
            "to", "in", "at", "than", "a", "an", "if", "else", "on", "every", "between",
            "above", "below", "under", "over", "up", "down", "north", "south", "east", "west");
    private static final Set<String> ONE = Set.of("a", "an", "one");
    private static final Pattern NAME_WORD = Pattern.compile("[a-z0-9_][a-z0-9_:./-]*");
    private static final int MAX_NAME_WORDS = 6;

    public record Split(List<List<Token>> parts, boolean disjunctive) {
    }

    private Literals() {
    }

    public static Optional<Double> number(List<Token> tokens) {
        if (tokens.size() != 1 || tokens.get(0).quoted() || !NUMBER.matcher(tokens.get(0).text()).matches()) {
            return Optional.empty();
        }
        return Optional.of(Double.parseDouble(tokens.get(0).text()));
    }

    public static Optional<Timespan> timespan(List<Token> tokens) {
        if (tokens.isEmpty() || tokens.size() > 2 || tokens.stream().anyMatch(Token::quoted)) {
            return Optional.empty();
        }
        String unit = tokens.get(tokens.size() - 1).text();
        if (tokens.size() == 1) {
            return Timespan.parse(1, unit);
        }
        String amount = tokens.get(0).text();
        if (ONE.contains(amount)) {
            return Timespan.parse(1, unit);
        }
        if (!NUMBER.matcher(amount).matches()) {
            return Optional.empty();
        }
        return Timespan.parse(Double.parseDouble(amount), unit);
    }

    public static Optional<BlockType> blockType(List<Token> tokens) {
        if (tokens.isEmpty() || tokens.size() > 4) {
            return Optional.empty();
        }
        List<String> words = new ArrayList<>();
        for (Token token : tokens) {
            if (token.quoted() || RESERVED.contains(token.text()) || !WORD.matcher(token.text()).matches()) {
                return Optional.empty();
            }
            words.add(token.text());
        }
        return Optional.of(BlockType.fromWords(String.join(" ", words)));
    }

    public static Optional<Expression> named(List<Token> tokens, SkType type) {
        Optional<String> words = words(tokens);
        if (words.isEmpty()) {
            return Optional.empty();
        }
        return NamedValues.parse(words.get(), type).map(value -> new ConstantExpression(type, value));
    }

    public static Optional<Expression> leveledEnchantment(List<Token> tokens) {
        if (tokens.size() < 2 || number(tokens.subList(tokens.size() - 1, tokens.size())).isEmpty()) {
            return Optional.empty();
        }
        return named(tokens, SkType.ENCHANTMENTTYPE);
    }

    public static Optional<Expression> reinterpret(Expression expression, SkType type) {
        Expression unwrapped = AmbiguousExpression.alternative(expression).orElse(expression);
        if (unwrapped instanceof ConstantExpression constant && constant.value() instanceof BlockType block) {
            String words = block.id().startsWith("minecraft:") ? block.path().replace('_', ' ') : block.id();
            return NamedValues.parse(words, type).map(value -> new ConstantExpression(type, value));
        }
        if (unwrapped instanceof ListExpression list && list.type() == SkType.BLOCKTYPE) {
            List<Expression> items = new ArrayList<>();
            for (Expression item : list.items()) {
                Optional<Expression> converted = reinterpret(item, type);
                if (converted.isEmpty()) {
                    return Optional.empty();
                }
                items.add(converted.get());
            }
            return Optional.of(new ListExpression(items, type, list.disjunctive()));
        }
        return Optional.empty();
    }

    private static Optional<String> words(List<Token> tokens) {
        if (tokens.isEmpty() || tokens.size() > MAX_NAME_WORDS) {
            return Optional.empty();
        }
        List<String> words = new ArrayList<>();
        for (Token token : tokens) {
            if (token.quoted() || !NAME_WORD.matcher(token.text()).matches()) {
                return Optional.empty();
            }
            words.add(token.text());
        }
        return Optional.of(String.join(" ", words));
    }

    public static Optional<Split> splitList(List<Token> tokens) {
        List<List<Token>> parts = new ArrayList<>();
        List<Token> current = new ArrayList<>();
        boolean disjunctive = false;
        boolean sawSeparator = false;
        for (Token token : tokens) {
            boolean comma = token.is(",");
            boolean or = token.is("or");
            boolean and = token.is("and");
            if (comma || or || and) {
                sawSeparator = true;
                disjunctive |= or;
                if (!current.isEmpty()) {
                    parts.add(List.copyOf(current));
                    current.clear();
                }
                continue;
            }
            current.add(token);
        }
        if (!current.isEmpty()) {
            parts.add(List.copyOf(current));
        }
        if (!sawSeparator || parts.size() < 2) {
            return Optional.empty();
        }
        return Optional.of(new Split(parts, disjunctive));
    }
}
