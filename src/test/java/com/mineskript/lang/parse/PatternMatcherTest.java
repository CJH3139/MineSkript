package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PatternMatcherTest {
    private static final ParseScope SCOPE = new ParseScope("t.ms", 1, null);

    private static final SlotResolver RESOLVER = (tokens, types, scope) -> {
        String joined = String.join(" ", tokens.stream().map(Token::text).toList());
        if (tokens.size() == 1 && tokens.get(0).quoted() && accepts(types, SkType.TEXT)) {
            return Optional.of(new ConstantExpression(SkType.TEXT, joined));
        }
        if (tokens.size() == 1 && tokens.get(0).text().matches("-?\\d+") && accepts(types, SkType.NUMBER)) {
            return Optional.of(new ConstantExpression(SkType.NUMBER, Double.parseDouble(joined)));
        }
        if (joined.equals("player") && accepts(types, SkType.PLAYER)) {
            return Optional.of(new ConstantExpression(SkType.PLAYER, "player"));
        }
        if (joined.equals("health of player") && accepts(types, SkType.NUMBER)) {
            return Optional.of(new ConstantExpression(SkType.NUMBER, 20.0));
        }
        return Optional.empty();
    };

    private static boolean accepts(List<SkType> types, SkType type) {
        return types.stream().anyMatch(t -> t.accepts(type));
    }

    private static Optional<Match> match(String pattern, String line) {
        return PatternMatcher.match(Pattern.compile(pattern), 3, Tokenizer.tokenize(line), RESOLVER, SCOPE);
    }

    @Test
    void matchesLiteralsCaseInsensitively() {
        Match match = match("on chat", "On CHAT").orElseThrow();
        assertEquals(3, match.patternIndex());
        assertTrue(match.slots().isEmpty());
    }

    @Test
    void rejectsExtraOrMissingTokens() {
        assertTrue(match("on chat", "on chat now").isEmpty());
        assertTrue(match("on chat", "on").isEmpty());
        assertTrue(match("on chat", "on \"chat\"").isEmpty());
    }

    @Test
    void optionalWordsMayBeAbsentOrPresent() {
        assertTrue(match("on [script] load", "on load").isPresent());
        assertTrue(match("on [script] load", "on script load").isPresent());
        assertTrue(match("on [script] load", "on script script load").isEmpty());
    }

    @Test
    void choicesRecordTags() {
        Match match = match("(press:press|hold:hold|release:release) [the] key %string%", "hold the key \"w\"").orElseThrow();
        assertEquals(Set.of("hold"), match.tags());
        assertEquals("w", ((ConstantExpression) match.slot(0)).value());
    }

    @Test
    void emptyChoiceBranchMatchesNothing() {
        assertTrue(match("%number% (is|are|) %number%", "1 2").isPresent());
        assertTrue(match("%number% (is|are|) %number%", "1 is 2").isPresent());
    }

    @Test
    void slotsBacktrackUntilTheRestOfThePatternFits() {
        Match match = match("%number% is less than %number%", "health of player is less than 6").orElseThrow();
        assertEquals(20.0, ((ConstantExpression) match.slot(0)).value());
        assertEquals(6.0, ((ConstantExpression) match.slot(1)).value());
    }

    @Test
    void optionalSlotLeavesNullWhenAbsent() {
        Match absent = match("[the] block [%-number% [(block|blocks)]] (above|below) [player]", "block below player").orElseThrow();
        assertNull(absent.slot(0));
        Match present = match("[the] block [%-number% [(block|blocks)]] (above|below) [player]", "the block 2 blocks above").orElseThrow();
        assertEquals(2.0, ((ConstantExpression) present.slot(0)).value());
    }

    @Test
    void slotTypeMustBeAcceptedByTheResolver() {
        assertTrue(match("wait %number%", "wait \"soon\"").isEmpty());
        assertTrue(match("wait %number/string%", "wait \"soon\"").isPresent());
    }

    @Test
    void possessivePatternMatchesSplitToken() {
        Match match = match("%player%'s health", "player's health").orElseThrow();
        assertEquals("player", ((ConstantExpression) match.slot(0)).value());
    }

    @Test
    void slotListHasOneEntryPerSlotInPatternOrder() {
        Match match = match("%number% between %number% and %number%", "5 between 1 and 9").orElseThrow();
        assertEquals(3, match.slots().size());
        assertEquals(1.0, ((ConstantExpression) match.slot(1)).value());
        assertEquals(9.0, ((ConstantExpression) match.slot(2)).value());
    }

    @Test
    void expressionInterfaceIsHonouredByConstants() {
        Expression constant = new ConstantExpression(SkType.NUMBER, 4.0);
        assertEquals(SkType.NUMBER, constant.type());
        assertEquals(4.0, constant.evaluate(null));
    }
}
