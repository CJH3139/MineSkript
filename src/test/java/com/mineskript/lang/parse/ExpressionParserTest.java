package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExpressionParserTest {
    private static final ParseScope SCOPE = new ParseScope("t.ms", 1, null);

    private static SyntaxRegistry registry() {
        SyntaxRegistry registry = new SyntaxRegistry();
        registry.addExpression(SkType.PLAYER, Tier.SIMPLE, (match, scope) -> Optional.of(new ConstantExpression(SkType.PLAYER, PlayerRef.LOCAL)), "[the] player");
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, (match, scope) -> Optional.of(new ConstantExpression(SkType.NUMBER, 20.0)), "[the] health of %player%", "%player%'s health");
        registry.addExpression(SkType.BLOCK, Tier.COMBINED, (match, scope) -> Optional.of(new ConstantExpression(SkType.BLOCK, new BlockValue("minecraft:stone"))), "[the] block below [player]");
        return registry;
    }

    private final ExpressionParser parser = new ExpressionParser(registry());
    private final Context context = new Context(new FakeGameBridge(), "t.ms", Map.of());

    private Expression parse(String text, SkType type) {
        return parser.parse(text, type, SCOPE).orElseThrow(() -> new AssertionError("did not parse: " + text));
    }

    @Test
    void parsesPlainStringsAsConstants() {
        Expression expression = parse("\"hello\"", SkType.TEXT);
        assertInstanceOf(ConstantExpression.class, expression);
        assertEquals("hello", expression.evaluate(context));
    }

    @Test
    void interpolatesExpressionsInsideStrings() {
        Expression expression = parse("\"hp %health of player% of %player% 100%%\"", SkType.TEXT);
        assertInstanceOf(TextLiteral.class, expression);
        assertEquals("hp 20 of Steve 100%", expression.evaluate(context));
    }

    @Test
    void unknownExpressionInsideStringIsASyntaxError() {
        SyntaxException error = assertThrows(SyntaxException.class, () -> parser.parse("\"%the dragon%\"", SkType.TEXT, SCOPE));
        assertEquals("unknown expression \"the dragon\" in string", error.getMessage());
    }

    @Test
    void parsesNumbersAndTimespans() {
        assertEquals(42.0, parse("42", SkType.NUMBER).evaluate(context));
        assertEquals(-1.5, parse("-1.5", SkType.NUMBER).evaluate(context));
        assertEquals(new Timespan(40), parse("2 seconds", SkType.TIMESPAN).evaluate(context));
        assertEquals(new Timespan(1), parse("tick", SkType.TIMESPAN).evaluate(context));
        assertEquals(new Timespan(20), parse("a second", SkType.TIMESPAN).evaluate(context));
        assertTrue(parser.parse("2 fortnights", SkType.TIMESPAN, SCOPE).isEmpty());
    }

    @Test
    void parsesDisjunctiveAndConjunctiveLists() {
        Expression list = parse("stone, dirt or grass block", SkType.BLOCKTYPE);
        ListExpression typed = assertInstanceOf(ListExpression.class, list);
        assertTrue(typed.disjunctive());
        assertEquals(SkType.BLOCKTYPE, typed.type());
        assertEquals(List.of(new BlockType("minecraft:stone"), new BlockType("minecraft:dirt"), new BlockType("minecraft:grass_block")), list.evaluate(context));
        ListExpression numbers = assertInstanceOf(ListExpression.class, parse("1 and 2", SkType.NUMBER));
        assertFalse(numbers.disjunctive());
    }

    @Test
    void listsMustBeHomogeneous() {
        assertTrue(parser.parse("1 or \"a\"", SkType.OBJECT, SCOPE).isEmpty());
    }

    @Test
    void registeredExpressionsWinOverBlockTypeWords() {
        assertEquals(PlayerRef.LOCAL, parse("player", SkType.OBJECT).evaluate(context));
        assertEquals(PlayerRef.LOCAL, parse("the player", SkType.PLAYER).evaluate(context));
        assertEquals(new BlockValue("minecraft:stone"), parse("block below player", SkType.BLOCK).evaluate(context));
        assertEquals(new BlockType("minecraft:oak_log"), parse("oak log", SkType.OBJECT).evaluate(context));
    }

    @Test
    void propertyPatternsResolveNestedSlotsWithoutLoopingForever() {
        assertEquals(20.0, parse("health of player", SkType.NUMBER).evaluate(context));
        assertEquals(20.0, parse("player's health", SkType.NUMBER).evaluate(context));
        assertEquals(20.0, parse("the health of the player", SkType.OBJECT).evaluate(context));
    }

    @Test
    void convertsWhenTheSlotTypeAllowsIt() {
        Expression text = parse("block below player", SkType.TEXT);
        assertInstanceOf(ConvertedExpression.class, text);
        assertEquals("stone", text.evaluate(context));
        Expression type = parse("block below player", SkType.BLOCKTYPE);
        assertEquals(new BlockType("minecraft:stone"), type.evaluate(context));
    }

    @Test
    void rejectsTypeMismatches() {
        assertTrue(parser.parse("\"x\"", SkType.NUMBER, SCOPE).isEmpty());
        assertTrue(parser.parse("player", SkType.NUMBER, SCOPE).isEmpty());
        assertTrue(parser.parse("5", SkType.BLOCKTYPE, SCOPE).isEmpty());
    }

    @Test
    void reservedWordsNeverBecomeBlockTypes() {
        assertTrue(parser.parse("player is", SkType.OBJECT, SCOPE).isEmpty());
        assertTrue(parser.parse("less than 6", SkType.OBJECT, SCOPE).isEmpty());
        assertTrue(parser.parse("", SkType.OBJECT, SCOPE).isEmpty());
    }

    @Test
    void repeatedParsesOfTheSameTextAgree() {
        assertEquals(20.0, parse("player's health", SkType.NUMBER).evaluate(context));
        assertEquals(20.0, parse("player's health", SkType.NUMBER).evaluate(context));
        assertEquals(20.0, parse("health of player", SkType.OBJECT).evaluate(context));
        parser.clearCache();
        assertEquals(20.0, parse("player's health", SkType.NUMBER).evaluate(context));
    }

    @Test
    void multiTypeSlotsTakeTheFirstAcceptingType() {
        Expression expression = parser.parse(Tokenizer.tokenize("5"), List.of(SkType.TEXT, SkType.NUMBER), SCOPE).orElseThrow();
        assertEquals(SkType.NUMBER, expression.type());
    }
}
