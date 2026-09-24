package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Stop")
@Description("Ends the current trigger immediately; nothing after it runs. Other triggers, including other runs of the same one that are waiting, are not affected. Also written exit, stop trigger or exit trigger.")
@Examples({"on key press of \"r\":",
        "\tif player is not holding diamond pickaxe:",
        "\t\tsend \"hold a diamond pickaxe\"",
        "\t\tstop",
        "\thold attack",
        "\twait 5 seconds",
        "\trelease attack"})
@Since("1.0.0-alpha")
public final class EffStop implements Statement {
    private final int line;

    private EffStop(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffStop(scope.line())), "(exit|stop) [trigger]");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return Flow.STOP;
    }
}
