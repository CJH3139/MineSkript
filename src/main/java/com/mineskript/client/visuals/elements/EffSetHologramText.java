package com.mineskript.client.visuals.elements;

import com.mineskript.client.TextColors;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Set Hologram Text")
@Description({"Changes the text of a hologram you spawned, given by its number from last spawned client entity. The text can use & colour codes, such as &a for green.",
        "Does nothing if the hologram no longer exists or the number belongs to an item or block display. This effect is tried before the general set effect, so set text of hologram always means this."})
@Examples({"on key press of \"h\":",
        "	spawn a hologram \"&7health\" at location(0.5, 66, 0.5)",
        "	set {-health hologram} to last spawned client entity",
        "",
        "on health change:",
        "	if {-health hologram} is set:",
        "		set text of hologram {-health hologram} to \"&c%health of player% hp\""})
@Since("1.0.0-alpha.9")
public final class EffSetHologramText implements Statement {
    private final int line;
    private final Expression handle;
    private final Expression text;

    private EffSetHologramText(int line, Expression handle, Expression text) {
        this.line = line;
        this.handle = handle;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffSetHologramText::create,
                "set [the] text of (hologram|client entity) %number% to %string%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffSetHologramText(scope.line(), match.slot(0), match.slot(1)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        int id = ClientEntityHandles.handle(handle.evaluate(context));
        String value = TextColors.colored(Converters.toText(text.evaluate(context), context));
        context.world().setClientEntityText(id, value);
        return Flow.CONTINUE;
    }
}
