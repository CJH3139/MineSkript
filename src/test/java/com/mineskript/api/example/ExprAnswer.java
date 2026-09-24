package com.mineskript.api.example;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("The Answer")
@Description("The answer to everything, the number 42.")
@Examples({"on load:",
        "\tshout \"%the answer to everything%\""})
@Since("1.0.0")
public final class ExprAnswer implements Expression {
    private ExprAnswer() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprAnswer()),
                "[the] answer to everything");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return 42.0;
    }
}
