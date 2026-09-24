package com.mineskript.client.hud.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Send Title")
@Description({
        "Shows a title, with an optional subtitle under it, in the middle of your own screen, like Skript's send title. Only you see it. for 5 seconds sets how long it stays, and with fade in and fade out how long it takes to appear and disappear. A time that is left out keeps the value of the last title shown (by default half a second to fade in, 3.5 seconds on screen and 1 second to fade out).",
        "send subtitle only sets the smaller line: it appears with the title on screen now, or with the next title, as in Skript. To show only a subtitle, send the title \" \" with it.",
        "MineSkript runs on your own client, so the title always goes to you: to player (or to me) may be written, as in Skript, or left out. Needs a world. Show Title is the older way to show one and still works."
})
@Examples({
        "on key press of \"t\":",
        "\tsend title \"Competition Started\" with subtitle \"Have fun, stay safe!\" for 5 seconds",
        "",
        "on death:",
        "\tsend title \"oops\" to player for 3 seconds with fade in 1 second and fade out 1 second",
        "",
        "on key press of \"y\":",
        "\tsend subtitle \"Party!\""
})
@Since("1.0.0-alpha.11")
public final class EffSendTitle implements Statement {
    private static final String TIMES = "[for %-timespan%] [with (fade-in|fade in|fadein) %-timespan%] "
            + "[[and] [with] (fade-out|fade out|fadeout) %-timespan%]";

    private final int line;
    private final Expression title;
    private final Expression subtitle;
    private final Expression stay;
    private final Expression fadeIn;
    private final Expression fadeOut;

    private EffSendTitle(int line, Expression title, Expression subtitle, Expression stay, Expression fadeIn,
            Expression fadeOut) {
        this.line = line;
        this.title = title;
        this.subtitle = subtitle;
        this.stay = stay;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.before(Priority.SIMPLE), EffSendTitle::create,
                "send title %string% [with subtitle %-string%] [to %players%] " + TIMES,
                "send subtitle %string% [to %players%] " + TIMES);
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        int shift = match.patternIndex();
        Expression title = shift == 0 ? match.slot(0) : null;
        Expression subtitle = match.slot(1 - shift);
        if ((title != null && title.isList()) || (subtitle != null && subtitle.isList())) {
            return Optional.empty();
        }
        return Optional.of(new EffSendTitle(scope.line(), title, subtitle, match.slot(3 - shift),
                match.slot(4 - shift), match.slot(5 - shift)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().sendTitle(text(title, context), text(subtitle, context), ticks(fadeIn, context),
                ticks(stay, context), ticks(fadeOut, context));
        return Flow.CONTINUE;
    }

    private static Optional<String> text(Expression expression, Context context) {
        if (expression == null) {
            return Optional.empty();
        }
        Object value = expression.evaluate(context);
        return value == None.NONE ? Optional.empty() : Optional.of(Converters.toText(value, context));
    }

    private static int ticks(Expression expression, Context context) {
        if (expression == null) {
            return -1;
        }
        return expression.evaluate(context) instanceof Timespan timespan ? timespan.ticks() : -1;
    }
}
