package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Loop Iteration")
@Description("Which pass of the innermost loop is running, starting at 1. Also written loop-counter, loop iteration or loop counter. Works in every kind of loop, including while. Using it outside a loop is a parse error.")
@Examples({
        "on key press of \"l\":",
        "	loop 3 times:",
        "		send \"pass %loop-iteration%\"",
        "",
        "on key press of \"l\":",
        "	loop online player names:",
        "		send \"%loop-iteration%. %loop-value%\""
})
@Since("1.0.0-alpha.2")
public final class ExprLoopIteration implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("loop-iteration is only available inside a loop");
            }
            return Optional.of(new ExprLoopIteration());
        }, "[the] (loop-iteration|loop-counter|loop iteration|loop counter)");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.currentLoop().iteration();
    }
}
