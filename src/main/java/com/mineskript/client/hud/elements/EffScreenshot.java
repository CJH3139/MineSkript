package com.mineskript.client.hud.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Take Screenshot")
@Description("Saves a screenshot to your screenshots folder, the same as pressing F2.")
@Examples({"on advancement:",
        "\twait 10 ticks",
        "\ttake a screenshot"})
@Since("1.0.0-alpha.2")
public final class EffScreenshot implements Statement {
    private final int line;

    private EffScreenshot(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffScreenshot(scope.line())),
                "take [a] screenshot");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.game().takeScreenshot();
        return Flow.CONTINUE;
    }
}
