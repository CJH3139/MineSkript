package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

@Name("Stop Script")
@Description({"Cancels every running or waiting trigger from one script file. The name is matched ignoring case, and .ms is added if you leave it off, so \"miner\" and \"Miner.ms\" both stop miner.ms. A script in a folder is named by its path, such as \"pvp/combat\". If it names the script you are in, the current trigger stops too; otherwise the current trigger carries on.",
        "The script stays loaded and its events keep firing. Unlike Stop All Scripts, it does not release keys or controls that the stopped script was holding, so release them yourself."})
@Examples({"on key press of \"x\":",
        "	stop script \"autominer\"",
        "	release attack",
        "	send \"autominer stopped\"",
        "",
        "on key press of \"z\":",
        "	stop script \"pvp/combat\""})
@Since({"1.0.0-alpha.5", "1.0.0-alpha.12"})
public final class EffStopScript implements Statement {
    private final int line;
    private final Expression name;

    private EffStopScript(int line, Expression name) {
        this.line = line;
        this.name = name;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new EffStopScript(scope.line(), match.slot(0))),
                "(stop|cancel) [the] script %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        String file = Converters.toText(name.evaluate(context), context).strip().replace('\\', '/');
        if (!file.toLowerCase(Locale.ROOT).endsWith(".ms")) {
            file = file + ".ms";
        }
        context.control().stopScript(file);
        return file.equalsIgnoreCase(context.triggerFile()) ? Flow.STOP : Flow.CONTINUE;
    }
}
