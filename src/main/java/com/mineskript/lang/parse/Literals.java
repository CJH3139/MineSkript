package com.mineskript.lang.parse;

import com.mineskript.lang.ast.BlockType;
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
