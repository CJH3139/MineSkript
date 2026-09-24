package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Exit Loop")
@Description({"Leaves the innermost loop straight away and carries on with the line after it. Also written stop loop, exit this loop and similar.",
        "Only allowed inside a loop or while section: anywhere else it is an error when the script loads."})
@Examples({"on key press of \"r\":",
        "\tloop 20 times:",
        "\t\tif block below player is air:",
        "\t\t\texit loop",
        "\t\tclick attack",
        "\t\twait 5 ticks",
        "\tsend \"stopped\""})
@Since("1.0.0-alpha.2")
public final class EffExitLoop implements Statement {
    private final int line;

    private EffExitLoop(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("exit loop is only available inside a loop");
            }
            return Optional.of(new EffExitLoop(scope.line()));
        }, "(exit|stop) [(the|this)] loop");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return Flow.EXIT_LOOP;
    }
}
