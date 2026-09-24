package com.mineskript.syntax;

import static com.mineskript.syntax.SyntaxTestSupport.holder;
import static com.mineskript.syntax.SyntaxTestSupport.scope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.Tokenizer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EventsTest {
    private Optional<Event> event(String text) {
        SyntaxTestSupport.SyntaxRegistryHolder holder = holder();
        return holder.registry().matchFirst(holder.registry().events(), Tokenizer.tokenize(text), holder.parser(), scope(null));
    }

    @Test
    void periodicEventsConvertTimespans() {
        assertEquals(new Event.Periodic(1), event("every tick").orElseThrow());
        assertEquals(new Event.Periodic(100), event("every 5 seconds").orElseThrow());
        assertEquals(new Event.Periodic(10), event("every 500 milliseconds").orElseThrow());
    }

    @Test
    void loadChatAndKeyEvents() {
        assertEquals(new Event.Load(), event("on load").orElseThrow());
        assertEquals(new Event.Load(), event("on script load").orElseThrow());
        Event chat = event("on chat").orElseThrow();
        assertInstanceOf(Event.Chat.class, chat);
        assertEquals(List.of("message"), chat.context().names());
        assertEquals(new Event.KeyPress("key.keyboard.r"), event("on key press of \"r\"").orElseThrow());
        assertEquals(new Event.KeyPress("key.keyboard.f6"), event("on press of \"F6\"").orElseThrow());
        assertEquals(new Event.KeyRelease("key.keyboard.left.shift"), event("on key release of \"left shift\"").orElseThrow());
    }

    @Test
    void unknownEventsAndKeys() {
        assertTrue(event("on sunrise").isEmpty());
        SyntaxException error = assertThrows(SyntaxException.class, () -> event("on key press of \"banana\""));
        assertEquals("unknown key \"banana\"", error.getMessage());
    }
}
