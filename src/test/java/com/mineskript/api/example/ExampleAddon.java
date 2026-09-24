package com.mineskript.api.example;

import com.mineskript.api.MineSkriptAddon;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class ExampleAddon implements MineSkriptAddon {
    @Override
    public String name() {
        return "Example Addon";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        registry.addEventValue(EventValue.of("ping count", SkType.NUMBER, "How many pings there have been."));
        registry.addEvent("Ping", (match, scope) -> Optional.of(new Event.State("example:ping")), "on ping")
                .values("ping count")
                .description("Fires when the example addon pings.")
                .examples("on ping:", "\tshout \"ping %event-ping count%\"")
                .since("1.0.0");
        EffShout.register(registry);
        ExprAnswer.register(registry);
    }
}
