package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.SkType;
import java.util.List;
import org.junit.jupiter.api.Test;

class PatternTest {
    @Test
    void splitsLiteralWordsIntoASequence() {
        Pattern pattern = Pattern.compile("on chat");
        PatternElement.Sequence root = assertInstanceOf(PatternElement.Sequence.class, pattern.root());
        assertEquals(List.of(new PatternElement.Literal("on"), new PatternElement.Literal("chat")), root.elements());
        assertEquals(0, pattern.slotCount());
    }

    @Test
    void parsesOptionalGroups() {
        Pattern pattern = Pattern.compile("on [script] load");
        PatternElement.Sequence root = (PatternElement.Sequence) pattern.root();
        assertEquals(3, root.elements().size());
        PatternElement.Optional optional = assertInstanceOf(PatternElement.Optional.class, root.elements().get(1));
        assertEquals(new PatternElement.Literal("script"), unwrap(optional.element()));
    }

    @Test
    void parsesChoicesWithTags() {
        Pattern pattern = Pattern.compile("(press|hold:hold|release) key");
        PatternElement.Sequence root = (PatternElement.Sequence) pattern.root();
        PatternElement.Choice choice = assertInstanceOf(PatternElement.Choice.class, root.elements().get(0));
        assertEquals(3, choice.branches().size());
        assertEquals("", choice.branches().get(0).tag());
        assertEquals("hold", choice.branches().get(1).tag());
        assertEquals(new PatternElement.Literal("hold"), unwrap(choice.branches().get(1).element()));
    }

    @Test
    void parsesSlotsWithTypesOptionalFlagAndIndex() {
        Pattern pattern = Pattern.compile("%objects% is %-number/string%");
        PatternElement.Sequence root = (PatternElement.Sequence) pattern.root();
        PatternElement.Slot first = assertInstanceOf(PatternElement.Slot.class, root.elements().get(0));
        assertEquals(0, first.index());
        assertEquals(List.of(SkType.OBJECT), first.types());
        assertFalse(first.optional());
        PatternElement.Slot second = assertInstanceOf(PatternElement.Slot.class, root.elements().get(2));
        assertEquals(1, second.index());
        assertEquals(List.of(SkType.NUMBER, SkType.TEXT), second.types());
        assertTrue(second.optional());
        assertEquals(2, pattern.slotCount());
    }

    @Test
    void nestsGroupsInsideGroups() {
        Pattern pattern = Pattern.compile("[the] block [%-number% [(block|blocks)]] (above|below) [player]");
        assertEquals(1, pattern.slotCount());
        PatternElement.Sequence root = (PatternElement.Sequence) pattern.root();
        assertEquals(5, root.elements().size());
    }

    @Test
    void emptyChoiceBranchIsAllowed() {
        Pattern pattern = Pattern.compile("(is|are|)");
        PatternElement.Choice choice = (PatternElement.Choice) ((PatternElement.Sequence) pattern.root()).elements().get(0);
        assertEquals(3, choice.branches().size());
    }

    @Test
    void rejectsUnbalancedBrackets() {
        assertThrows(IllegalArgumentException.class, () -> Pattern.compile("on [script load"));
        assertThrows(IllegalArgumentException.class, () -> Pattern.compile("(a|b"));
        assertThrows(IllegalArgumentException.class, () -> Pattern.compile("%number"));
        assertThrows(IllegalArgumentException.class, () -> Pattern.compile("%dragon%"));
    }

    private static PatternElement unwrap(PatternElement element) {
        if (element instanceof PatternElement.Sequence sequence && sequence.elements().size() == 1) {
            return sequence.elements().get(0);
        }
        return element;
    }
}
