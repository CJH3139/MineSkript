package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mineskript.lang.ast.SkType;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExpressionPatternGuardTest {
    private static final String REASON = ". Such a pattern offers its slot the entire token list it is already parsing, so every nested"
            + " expression re-parses the same tokens and parsing becomes exponential, freezing the client"
            + " instead of failing. Add a required literal or a second required slot so at least one token is"
            + " always consumed outside the slot.";

    @Test
    void rejectsExpressionPatternsThatCanMatchAsASlotAlone() {
        for (String source : List.of("%string% [as text]", "[the] %string%", "%string%")) {
            SyntaxRegistry registry = new SyntaxRegistry();
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.empty(), source));
            assertEquals("expression pattern can match as a single slot with nothing else required: " + source + REASON,
                    thrown.getMessage());
            assertEquals(List.of(), registry.expressions(Tier.SIMPLE));
        }
    }

    @Test
    void acceptsExpressionPatternsThatAlwaysConsumeATokenOutsideTheSlot() {
        SyntaxRegistry registry = new SyntaxRegistry();
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, (match, scope) -> Optional.empty(), "%string%'s length", "length of %string%");
        assertEquals(1, registry.expressions(Tier.PROPERTY).size());
    }

    @Test
    void theDefaultRegistryBuildsWithoutTrippingTheGuard() {
        SyntaxRegistry registry = DefaultSyntax.registry();
        assertNotNull(registry);
        for (Tier tier : Tier.values()) {
            assertFalse(registry.expressions(tier).isEmpty());
            for (ExpressionEntry entry : registry.expressions(tier)) {
                for (Pattern pattern : entry.patterns()) {
                    assertFalse(PatternElement.canMatchAsSlotAlone(pattern.root()), pattern.source());
                }
            }
        }
    }
}
