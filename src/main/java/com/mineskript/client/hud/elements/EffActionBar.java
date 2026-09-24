package com.mineskript.client.hud.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Action Bar")
@Description({
        "Shows text just above your hotbar, like Skript's send action bar. Only you see it.",
        "MineSkript runs on your own client, so the action bar is always yours: to player (or to me) may be written, as in Skript, or left out. Needs a world. show action bar is the older spelling and still works."
})
@Examples({
        "on move:",
        "\tsend action bar \"x %round player's x-coordinate%  z %round player's z-coordinate%\"",
        "",
        "on key press of \"h\":",
        "\tsend the actionbar with text \"hello\" to player"
})
@Since("1.0.0-alpha.11")
public final class EffActionBar implements Statement {
    private final int line;
    private final Expression text;

    private EffActionBar(int line, Expression text) {
        this.line = line;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.before(Priority.SIMPLE), EffActionBar::create,
                "send [the] (action bar|actionbar) [with text] %string% [to %players%]");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffActionBar(scope.line(), match.slot(0)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().showActionBar(Converters.toText(text.evaluate(context), context));
        return Flow.CONTINUE;
    }
}
