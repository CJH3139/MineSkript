package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Wait")
@Description({"Pauses the trigger for a length of time, then carries on from the next line. The game keeps running normally while a trigger waits. Times can be given in ticks, milliseconds, seconds, minutes or hours, such as wait 20 ticks, wait 1.5 seconds or wait a tick. Halt is another word for wait.",
        "A tick is 50 milliseconds; any time is rounded to whole ticks and waits at least 1 tick. A function used as a value cannot wait."})
@Examples({"on key press of \"r\":",
        "\thold use",
        "\twait 2 seconds",
        "\trelease use",
        "",
        "every 1 minute:",
        "\tset {_before} to health of player",
        "\twait 5 seconds",
        "\tif health of player is less than {_before}:",
        "\t\tsend \"still taking damage\""})
@Since("1.0.0-alpha")
public final class EffWait implements Statement {
    private final int line;
    private final Expression duration;

    private EffWait(int line, Expression duration) {
        this.line = line;
        this.duration = duration;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new EffWait(scope.line(), match.slot(0))),
                "(wait|halt) [for] %timespan%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        Timespan timespan = (Timespan) duration.evaluate(context);
        return new Flow.Wait(timespan.ticks());
    }
}
