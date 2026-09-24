package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.LoopKind;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Loop Index")
@Description({
        "The index of the current entry when looping a list variable, as text: looping {homes::*} where {homes::alex} is set gives alex for that entry. Indices are lowercase, because variable names are case-insensitive.",
        "In a loop over any other list it is the position of the entry, and in loop N times the pass number, both as text starting at 1. Also written loop index. Using it outside a loop, or inside a while loop, is a parse error."
})
@Examples({
        "on key press of \"h\":",
        "\tloop {homes::*}:",
        "\t\tsend \"%loop-index%: %loop-value%\""
})
@Since("1.0.0-alpha.8")
public final class ExprLoopIndex implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("loop-index is only available inside a loop");
            }
            if (scope.loop() == LoopKind.WHILE) {
                throw new SyntaxException("loop-index is not available inside while");
            }
            return Optional.of(new ExprLoopIndex());
        }, "[the] (loop-index|loop index)");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.currentLoop().index();
    }
}
