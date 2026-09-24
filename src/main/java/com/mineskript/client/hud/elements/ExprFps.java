package com.mineskript.client.hud.elements;

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

@Name("FPS")
@Description("The game's current frames per second, as a whole number. Also written frame rate. Works without a world.")
@Examples({
        "on key press of \"f\":",
        "\tsend \"%fps% fps\""
})
@Since("1.0.0-alpha.2")
public final class ExprFps implements Expression {
    private ExprFps() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprFps()), "[the] (fps|frame rate)");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.game().fps();
    }
}
