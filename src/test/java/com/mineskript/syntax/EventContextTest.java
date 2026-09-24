package com.mineskript.syntax;

import static com.mineskript.syntax.SyntaxTestSupport.event;
import static com.mineskript.syntax.SyntaxTestSupport.scope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.EventInfo;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EventContextTest {
    private final SyntaxTestSupport.SyntaxRegistryHolder holder = SyntaxTestSupport.holder();
    private final SyntaxRegistry registry = holder.registry();

    private Event parsedEvent(EventInfo info) {
        String header = info.examples().get(0);
        return event(header.substring(0, header.length() - 1));
    }

    private Optional<?> cancel(Event event) {
        return registry.matchFirst(registry.effects(), Tokenizer.tokenize("cancel event"), holder.parser(), scope(event));
    }

    @Test
    void aValueTheEventDoesNotDeclareIsAParseError() {
        ParsedScript script = new Parser(DefaultSyntax.registry())
                .parse("t.ms", "on damage:\n    send \"%event-healed%\"\non move:\n    send \"%event-item%\"\n");
        assertEquals(2, script.errors().size());
        assertTrue(script.errors().get(0).toString().contains("event-healed is not available in this event"));
        assertTrue(script.errors().get(1).toString().contains("event-item is not available in this event"));
    }

    @Test
    void aDeclaredValueParsesWithItsDeclaredType() {
        assertEquals(SkType.NUMBER, holder.parser().parse("event-damage", SkType.OBJECT, scope(event("on damage")))
                .orElseThrow().type());
        assertEquals(SkType.ITEM, holder.parser().parse("event-item", SkType.OBJECT, scope(event("on durability below 5")))
                .orElseThrow().type());
    }

    @Test
    void everyEventValueParsesExactlyInTheEventsThatDeclareIt() {
        List<String> wrong = new ArrayList<>();
        for (EventInfo info : registry.eventInfos()) {
            Event event = parsedEvent(info);
            for (EventValue value : registry.eventValues()) {
                boolean declared = info.context().provides(value.name());
                boolean parses;
                try {
                    parses = holder.parser().parse(value.syntax(), value.type(), scope(event)).isPresent();
                } catch (SyntaxException error) {
                    parses = false;
                }
                if (parses != declared) {
                    wrong.add(info.name() + " / " + value.syntax() + (declared ? " should parse" : " should not parse"));
                }
            }
        }
        assertEquals(List.of(), wrong);
    }

    @Test
    void theEffectCommandMessageIsKept() {
        SyntaxException error = assertThrows(SyntaxException.class,
                () -> holder.parser().parse("event-damage", SkType.NUMBER, scope(new Event.EffectCommand())));
        assertEquals("event-damage needs an event, and an effect command typed in chat has none", error.getMessage());
    }

    @Test
    void cancelEventIsAllowedExactlyInCancellableEvents() {
        List<String> cancellable = new ArrayList<>();
        for (EventInfo info : registry.eventInfos()) {
            Event event = parsedEvent(info);
            if (info.context().cancellable()) {
                cancellable.add(info.name());
                assertTrue(cancel(event).isPresent(), info.name());
            } else {
                SyntaxException error = assertThrows(SyntaxException.class, () -> cancel(event), info.name());
                assertEquals("only \"on chat send\" and \"on command send\" can be cancelled", error.getMessage());
            }
        }
        assertEquals(List.of("Chat Send", "Command Send"), cancellable);
    }

    @Test
    void parsedEventsCarryTheirDeclaredContext() {
        assertEquals(List.of("message"), event("on chat").context().names());
        assertTrue(event("on chat send").context().cancellable());
        assertEquals(List.of("health change", "old health"), event("on health change").context().names());
        assertEquals(List.of("durability", "item"), event("on durability below 3").context().names());
        assertEquals(List.of(), event("on load").context().names());
    }

    @Test
    void anEventCannotDeclareAnUndefinedValue() {
        SyntaxRegistry fresh = new SyntaxRegistry();
        fresh.addEventValue(EventValue.of("known", SkType.NUMBER, "a value"));
        EventInfo info = fresh.addEvent("Test", (match, scope) -> Optional.of(new Event.Load()), "on test");
        assertThrows(IllegalArgumentException.class, () -> info.values("unknown"));
        assertThrows(IllegalArgumentException.class,
                () -> fresh.addEventValue(EventValue.of("known", SkType.TEXT, "again")));
    }
}
