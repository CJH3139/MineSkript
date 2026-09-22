package com.mineskript.syntax.events;

import com.mineskript.game.KeyNames;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class EventSyntax {
    private EventSyntax() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEvent(EventSyntax::periodic, "every %timespan%");
        registry.addEvent((match, scope) -> Optional.of(new Event.Load()), "on [script] load");
        registry.addEvent((match, scope) -> Optional.of(new Event.Chat()), "on chat");
        registry.addEvent((match, scope) -> Optional.of(new Event.KeyPress(KeyNames.keyIdOf(match.slot(0)))), "on [key] press of %string%");
        registry.addEvent((match, scope) -> Optional.of(new Event.KeyRelease(KeyNames.keyIdOf(match.slot(0)))), "on [key] release of %string%");
    }

    private static Optional<Event> periodic(Match match, ParseScope scope) {
        if (!(match.slot(0) instanceof ConstantExpression constant) || !(constant.value() instanceof Timespan timespan)) {
            throw new SyntaxException("\"every\" needs a fixed timespan like \"every 5 seconds\"");
        }
        return Optional.of(new Event.Periodic(timespan.ticks()));
    }
}
