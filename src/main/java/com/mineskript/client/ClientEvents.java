package com.mineskript.client;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.EventInfo;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static EventInfo state(SyntaxRegistry registry, String displayName, String pattern, String name) {
        return registry.addEvent(displayName, (match, scope) -> Optional.of(new Event.State(name)), pattern);
    }
}
