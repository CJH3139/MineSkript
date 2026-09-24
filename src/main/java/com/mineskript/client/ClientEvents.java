package com.mineskript.client;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.EventInfo;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

/** Registration shared by the client feature modules' events. Not a syntax element itself. */
public final class ClientEvents {
    private ClientEvents() {
    }

    /**
     * Registers an event that the dispatcher fires by name ({@link Event.State}), such as {@code "jump"} for
     * {@code on jump}. Document it with the chained calls on the returned {@link EventInfo}.
     */
    public static EventInfo state(SyntaxRegistry registry, String displayName, String pattern, String name) {
        return registry.addEvent(displayName, (match, scope) -> Optional.of(new Event.State(name)), pattern);
    }
}
