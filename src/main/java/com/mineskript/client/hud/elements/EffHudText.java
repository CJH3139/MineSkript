package com.mineskript.client.hud.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Show Title")
@Description({"Shows text on your own screen: show title puts large text in the middle, show subtitle sets the smaller line under it, and show action bar shows text just above the hotbar. A title fades in over half a second, stays for about 3.5 seconds and fades out over 1 second. Only you see it.",
        "A subtitle is only drawn while a title is on screen, so show it together with a title. Needs a world."})
@Examples({"every 5 seconds:",
        "\tif health of player is less than 6:",
        "\t\tshow subtitle \"%health of player% hp left\"",
        "\t\tshow title \"low health\"",
        "",
        "on move:",
        "\tshow action bar \"x %round player's x-coordinate%  z %round player's z-coordinate%\""})
@Since("1.0.0-alpha.2")
public final class EffHudText implements Statement {
    private enum Target {
        TITLE,
        SUBTITLE,
        ACTION_BAR
    }

    private final int line;
    private final Target target;
    private final Expression text;

    private EffHudText(int line, Target target, Expression text) {
        this.line = line;
        this.target = target;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffHudText::create,
                "show title %string%",
                "show subtitle %string%",
                "show (action bar|actionbar) %string%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffHudText(scope.line(), Target.values()[match.patternIndex()], match.slot(0)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        String value = Converters.toText(text.evaluate(context), context);
        switch (target) {
            case TITLE -> game.showTitle(value);
            case SUBTITLE -> game.showSubtitle(value);
            case ACTION_BAR -> game.showActionBar(value);
        }
        return Flow.CONTINUE;
    }
}
