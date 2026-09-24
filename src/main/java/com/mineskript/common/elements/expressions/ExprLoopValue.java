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

@Name("Loop Value")
@Description("The current value of the innermost loop. In loop N times it is the pass number (1, 2, 3...); in a loop over a list it is the current element, and in a loop over a list variable such as {homes::*} it is the current entry's value (loop-index gives its index). Its type is whatever the list holds. Using it outside a loop, or inside a while loop, is a parse error.")
@Examples({
        "on key press of \"l\":",
        "	loop \"red,green,blue\" split at \",\":",
        "		send \"colour: %loop-value%\"",
        "",
        "on key press of \"l\":",
        "	loop stone, dirt and gravel:",
        "		if block below player is loop-value:",
        "			send \"standing on %loop-value%\""
})
@Since("1.0.0-alpha.2")
public final class ExprLoopValue implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.OBJECT, Tier.SIMPLE, (match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("loop-value is only available inside a loop");
            }
            if (scope.loop() == LoopKind.WHILE) {
                throw new SyntaxException("loop-value is not available inside while");
            }
            return Optional.of(new ExprLoopValue());
        }, "[the] [current] loop-value");
    }

    @Override
    public SkType type() {
        return SkType.OBJECT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.currentLoop().value();
    }
}
