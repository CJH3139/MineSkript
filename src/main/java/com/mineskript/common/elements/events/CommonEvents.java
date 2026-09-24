package com.mineskript.common.elements.events;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class CommonEvents {
    private CommonEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEvent("Periodic", CommonEvents::periodic, "every %timespan%")
                .description("Runs repeatedly at a fixed interval, counted in client ticks (20 per second) while you are in a world. The timespan must be written as a fixed value such as every 5 seconds or every tick, and it is rounded to whole ticks with a minimum of 1 tick. The tick counter only advances while a world is loaded and restarts when all scripts are reloaded, so nothing runs on the title screen.",
                        "A timespan built from a variable or expression is rejected when the script loads.")
                .examples("every 5 seconds:",
                        "\tif health of player is less than 6:",
                        "\t\tshow title \"low health\"",
                        "",
                        "every tick:",
                        "\tif player is on fire:",
                        "\t\tsend \"you are burning\"")
                .since("1.0.0-alpha");
        registry.addEvent("Script Load", (match, scope) -> Optional.of(new Event.Load()), "on [script] load")
                .description("Runs once when the script is loaded: at game start, when every script is reloaded, and for a single file when that file is added or reloaded with /ms reload. At game start there is usually no world yet, so lines that need the player fail with a no world error, and anything after a wait is dropped because waits are cleared while no world is loaded. Use it to set up variables; use on world join for things that need the player.")
                .examples("on load:",
                        "\tif {greeting} is not set:",
                        "\t\tset {greeting} to \"hello\"",
                        "",
                        "on script load:",
                        "\tset {-kills} to 0")
                .since("1.0.0-alpha");
    }

    private static Optional<Event> periodic(Match match, ParseScope scope) {
        if (!(match.slot(0) instanceof ConstantExpression constant) || !(constant.value() instanceof Timespan timespan)) {
            throw new SyntaxException("\"every\" needs a fixed timespan like \"every 5 seconds\"");
        }
        return Optional.of(new Event.Periodic(timespan.ticks()));
    }
}
