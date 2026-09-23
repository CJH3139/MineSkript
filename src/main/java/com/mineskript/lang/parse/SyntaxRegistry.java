package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SyntaxRegistry {
    private final List<SyntaxEntry<Event>> events = new ArrayList<>();
    private final List<SyntaxEntry<Statement>> effects = new ArrayList<>();
    private final List<SyntaxEntry<Condition>> conditions = new ArrayList<>();
    private final Map<Tier, List<ExpressionEntry>> expressions = new EnumMap<>(Tier.class);

    public SyntaxRegistry() {
        for (Tier tier : Tier.values()) {
            expressions.put(tier, new ArrayList<>());
        }
    }

    public void addEvent(SyntaxFactory<Event> factory, String... patterns) {
        events.add(new SyntaxEntry<>(compile(patterns), factory));
    }

    public void addEffect(SyntaxFactory<Statement> factory, String... patterns) {
        effects.add(new SyntaxEntry<>(compile(patterns), factory));
    }

    public void addCondition(SyntaxFactory<Condition> factory, String... patterns) {
        conditions.add(new SyntaxEntry<>(compile(patterns), factory));
    }

    public void addExpression(SkType returnType, Tier tier, SyntaxFactory<Expression> factory, String... patterns) {
        expressions.get(tier).add(new ExpressionEntry(compileExpressionPatterns(patterns), returnType, tier, factory));
    }

    public List<SyntaxEntry<Event>> events() {
        return events;
    }

    public List<SyntaxEntry<Statement>> effects() {
        return effects;
    }

    public List<SyntaxEntry<Condition>> conditions() {
        return conditions;
    }

    public List<ExpressionEntry> expressions(Tier tier) {
        return expressions.get(tier);
    }

    public <T> Optional<T> matchFirst(List<SyntaxEntry<T>> entries, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        for (SyntaxEntry<T> entry : entries) {
            Optional<T> result = matchPatterns(entry.patterns(), entry.factory(), tokens, resolver, scope);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    public Optional<Expression> matchEntry(ExpressionEntry entry, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        return matchPatterns(entry.patterns(), entry.factory(), tokens, resolver, scope);
    }

    private static <T> Optional<T> matchPatterns(List<Pattern> patterns, SyntaxFactory<T> factory, List<Token> tokens, SlotResolver resolver, ParseScope scope) {
        for (int i = 0; i < patterns.size(); i++) {
            Optional<Match> match = PatternMatcher.match(patterns.get(i), i, tokens, resolver, scope);
            if (match.isEmpty()) {
                continue;
            }
            Optional<T> result = factory.create(match.get(), scope);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private static List<Pattern> compile(String... patterns) {
        return Arrays.stream(patterns).map(Pattern::compile).toList();
    }

    private static List<Pattern> compileExpressionPatterns(String... patterns) {
        List<Pattern> compiled = compile(patterns);
        for (Pattern pattern : compiled) {
            if (PatternElement.canMatchAsSlotAlone(pattern.root())) {
                throw new IllegalArgumentException("expression pattern can match as a single slot with nothing else required: "
                        + pattern.source()
                        + ". Such a pattern offers its slot the entire token list it is already parsing, so every nested"
                        + " expression re-parses the same tokens and parsing becomes exponential, freezing the client"
                        + " instead of failing. Add a required literal or a second required slot so at least one token is"
                        + " always consumed outside the slot.");
            }
        }
        return compiled;
    }
}
