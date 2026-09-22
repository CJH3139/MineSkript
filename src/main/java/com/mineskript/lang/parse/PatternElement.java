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

    record Slot(int index, List<SkType> types, boolean optional) implements PatternElement {
    }
}
