package com.mineskript.client.hud.elements;

import com.mineskript.client.TextColors;
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
        "A subtitle is only drawn while a title is on screen, so show it together with a title. Needs a world.",
        "Skript's spellings, send title (with a subtitle and times) and send action bar, do the same and are described under Send Title and Action Bar.",
        "Colour codes work like in Skript: &c for red, &l for bold, &r to reset and the rest of the & codes."})
@Examples({"every 5 seconds:",
        "	if health of player is less than 6:",
        "		show subtitle \"%health of player% hp left\"",
        "		show title \"low health\"",
        "",
        "on move:",
        "	show action bar \"x %round player's x-coordinate%  z %round player's z-coordinate%\""})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.12"})
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
        String value = TextColors.colored(Converters.toText(text.evaluate(context), context));
        switch (target) {
            case TITLE -> game.showTitle(value);
            case SUBTITLE -> game.showSubtitle(value);
            case ACTION_BAR -> game.showActionBar(value);
        }
        return Flow.CONTINUE;
    }
}
