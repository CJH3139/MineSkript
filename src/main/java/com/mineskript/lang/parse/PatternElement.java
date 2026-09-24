package com.mineskript.lang.parse;

import com.mineskript.lang.ast.SkType;
import java.util.List;

public sealed interface PatternElement {
    record Literal(String word) implements PatternElement {
    }

    record Sequence(List<PatternElement> elements) implements PatternElement {
    }

    record Optional(PatternElement element) implements PatternElement {
    }

    record Choice(List<Branch> branches) implements PatternElement {
    }

    record Branch(String tag, PatternElement element) {
    }

    record Slot(int index, List<SkType> types, boolean optional, boolean condition, boolean raw)
            implements PatternElement {
        public Slot(int index, List<SkType> types, boolean optional) {
            this(index, types, optional, false, false);
        }
    }

    static int minimum(PatternElement element) {
        return switch (element) {
            case Literal ignored -> 1;
            case Sequence sequence -> {
                int total = 0;
                for (PatternElement child : sequence.elements()) {
                    total += minimum(child);
                }
                yield total;
            }
            case Optional ignored -> 0;
            case Choice choice -> {
                int best = Integer.MAX_VALUE;
                for (Branch branch : choice.branches()) {
                    best = Math.min(best, minimum(branch.element()));
                }
                yield best;
            }
            case Slot slot -> slot.optional() ? 0 : 1;
        };
    }

    static boolean canMatchAsSlotAlone(PatternElement element) {
        return switch (element) {
            case Literal ignored -> false;
            case Slot ignored -> true;
            case Optional optional -> canMatchAsSlotAlone(optional.element());
            case Choice choice -> {
                for (Branch branch : choice.branches()) {
                    if (canMatchAsSlotAlone(branch.element())) {
                        yield true;
                    }
                }
                yield false;
            }
            case Sequence sequence -> {
                List<PatternElement> elements = sequence.elements();
                for (int i = 0; i < elements.size(); i++) {
                    if (canMatchAsSlotAlone(elements.get(i)) && everyOtherElementCanMatchNothing(elements, i)) {
                        yield true;
                    }
                }
                yield false;
            }
        };
    }

    private static boolean everyOtherElementCanMatchNothing(List<PatternElement> elements, int index) {
        for (int i = 0; i < elements.size(); i++) {
            if (i != index && minimum(elements.get(i)) != 0) {
                return false;
            }
        }
        return true;
    }
}
