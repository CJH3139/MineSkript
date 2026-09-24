package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class PatternMatcher {
    private interface Continuation {
        boolean run(int position);
    }

    private static final ThreadLocal<Boolean> EXHAUSTIVE = ThreadLocal.withInitial(() -> false);

    private final Pattern pattern;
    private final List<Token> tokens;
    private final SlotResolver resolver;
    private final ParseScope scope;
    private final Expression[] slots;
    private final Deque<String> tags = new ArrayDeque<>();

    private PatternMatcher(Pattern pattern, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        this.pattern = pattern;
        this.tokens = tokens;
        this.resolver = resolver;
        this.scope = scope;
        this.slots = new Expression[pattern.slotCount()];
    }

    static boolean exhaustive() {
        return EXHAUSTIVE.get();
    }

    static <T> T exhaustively(Supplier<T> action) {
        boolean outer = EXHAUSTIVE.get();
        EXHAUSTIVE.set(true);
        try {
            return action.get();
        } finally {
            EXHAUSTIVE.set(outer);
        }
    }

    public static Optional<Match> match(Pattern pattern, int patternIndex, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        if (!EXHAUSTIVE.get() && !pattern.mayMatch(tokens)) {
            return Optional.empty();
        }
        PatternMatcher matcher = new PatternMatcher(pattern, tokens, resolver, scope);
        boolean matched = matcher.match(pattern.root(), 0, 0, position -> position == tokens.size());
        if (!matched) {
            return Optional.empty();
        }
        Set<String> tags = new HashSet<>(matcher.tags);
        return Optional.of(new Match(Arrays.asList(matcher.slots), tags, patternIndex));
    }

    private boolean match(PatternElement element, int position, int reserve, Continuation next) {
        return switch (element) {
            case PatternElement.Literal literal -> position < tokens.size() && tokens.get(position).is(literal.word()) && next.run(position + 1);
            case PatternElement.Sequence sequence -> matchSequence(sequence, 0, position, reserve, next);
            case PatternElement.Optional optional -> match(optional.element(), position, reserve, next) || next.run(position);
            case PatternElement.Choice choice -> matchChoice(choice, position, reserve, next);
            case PatternElement.Slot slot -> matchSlot(slot, position, reserve, next);
        };
    }

    private boolean matchSequence(PatternElement.Sequence sequence, int index, int position, int reserve, Continuation next) {
        List<PatternElement> elements = sequence.elements();
        if (index == elements.size()) {
            return next.run(position);
        }
        int rest = reserve + pattern.suffixMinimum(sequence, index + 1);
        return match(elements.get(index), position, rest, p -> matchSequence(sequence, index + 1, p, reserve, next));
    }

    private boolean matchChoice(PatternElement.Choice choice, int position, int reserve, Continuation next) {
        for (PatternElement.Branch branch : choice.branches()) {
            boolean tagged = !branch.tag().isEmpty();
            if (tagged) {
                tags.push(branch.tag());
            }
            if (match(branch.element(), position, reserve, next)) {
                return true;
            }
            if (tagged) {
                tags.pop();
            }
        }
        return false;
    }

    private boolean matchSlot(PatternElement.Slot slot, int position, int reserve, Continuation next) {
        if (slot.raw()) {
            if (position < tokens.size() && tokens.get(position).quoted()) {
                slots[slot.index()] = new ConstantExpression(SkType.TEXT, tokens.get(position).text());
                if (next.run(position + 1)) {
                    return true;
                }
                slots[slot.index()] = null;
            }
            return slot.optional() && next.run(position);
        }
        for (int end = tokens.size() - reserve; end > position; end--) {
            List<Token> part = tokens.subList(position, end);
            Optional<Expression> expression = slot.condition()
                    ? resolver.resolveCondition(part, scope)
                    : resolver.resolve(part, slot.types(), scope);
            if (expression.isEmpty()) {
                continue;
            }
            slots[slot.index()] = expression.get();
            if (next.run(end)) {
                return true;
            }
            slots[slot.index()] = null;
        }
        return slot.optional() && next.run(position);
    }
}
