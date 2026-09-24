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

@Name("Stop All Scripts")
@Description({"Cancels every trigger that is running or waiting, in every script, and releases every key, attack and use control that scripts are holding. The trigger that runs it stops too. Scripts stay loaded, so their events fire again the next time they happen. Cancel all running scripts also works.",
        "Nothing after stop all scripts runs in the same trigger, so put any message before it."})
@Examples({"on key press of \"f12\":",
        "	show action bar \"stopping all scripts\"",
        "	stop all scripts"})
@Since("1.0.0-alpha.5")
public final class EffStopAllScripts implements Statement {
    private final int line;

    private EffStopAllScripts(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffStopAllScripts(scope.line())),
                "(stop|cancel) all [running] scripts");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.control().stopAll();
        return Flow.STOP;
    }
}
