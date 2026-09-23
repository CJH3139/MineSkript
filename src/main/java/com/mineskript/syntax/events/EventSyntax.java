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
        registry.addEvent((match, scope) -> Optional.of(new Event.ChatSend()), "on chat send");
        registry.addEvent((match, scope) -> Optional.of(new Event.CommandSend()), "on command send");
        registry.addEvent((match, scope) -> Optional.of(new Event.KeyPress(KeyNames.keyIdOf(match.slot(0)))), "on [key] press of %string%");
        registry.addEvent((match, scope) -> Optional.of(new Event.KeyRelease(KeyNames.keyIdOf(match.slot(0)))), "on [key] release of %string%");
        state(registry, "on move", "move");
        state(registry, "on jump", "jump");
        state(registry, "on land", "land");
        state(registry, "on sneak", "sneak");
        state(registry, "on (stop sneaking|unsneak)", "unsneak");
        state(registry, "on sprint", "sprint");
        state(registry, "on (stop sprinting|unsprint)", "unsprint");
        state(registry, "on damage", "damage");
        state(registry, "on heal", "heal");
        state(registry, "on death", "death");
        state(registry, "on respawn", "respawn");
        state(registry, "on hunger change", "hunger");
        state(registry, "on (held item change|item switch)", "held");
        state(registry, "on inventory change", "inventory");
        state(registry, "on start using item", "use start");
        state(registry, "on stop using item", "use stop");
        state(registry, "on level change", "level");
        state(registry, "on gamemode change", "gamemode");
        state(registry, "on weather change", "weather");
        state(registry, "on screen open", "screen open");
        state(registry, "on screen close", "screen close");
        state(registry, "on world join", "join");
        state(registry, "on world leave", "leave");
        state(registry, "on (consume|eat|drink)", "consume");
        state(registry, "on (item break|tool break)", "item break");
        state(registry, "on (xp change|experience change)", "xp");
        state(registry, "on effect gain", "effect gain");
        state(registry, "on (effect lose|effect loss)", "effect lose");
        state(registry, "on mount", "mount");
        state(registry, "on dismount", "dismount");
        state(registry, "on dimension change", "dimension");
        state(registry, "on player join", "player join");
        state(registry, "on player leave", "player leave");
    }

    private static void state(SyntaxRegistry registry, String pattern, String name) {
        registry.addEvent((match, scope) -> Optional.of(new Event.State(name)), pattern);
    }

    private static Optional<Event> periodic(Match match, ParseScope scope) {
        if (!(match.slot(0) instanceof ConstantExpression constant) || !(constant.value() instanceof Timespan timespan)) {
            throw new SyntaxException("\"every\" needs a fixed timespan like \"every 5 seconds\"");
        }
        return Optional.of(new Event.Periodic(timespan.ticks()));
    }
}
