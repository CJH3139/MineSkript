package com.mineskript.syntax;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.ExpressionParser;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tokenizer;
import com.mineskript.lang.runtime.Context;
import com.mineskript.syntax.DefaultSyntax;
import java.util.Map;

public final class SyntaxTestSupport {
    public record SyntaxRegistryHolder(SyntaxRegistry registry, ExpressionParser parser) {
    }

    private SyntaxTestSupport() {
    }

    public static SyntaxRegistry registry() {
        SyntaxRegistry registry = new SyntaxRegistry();
        DefaultSyntax.registerAll(registry);
        return registry;
    }

    public static SyntaxRegistryHolder holder() {
        SyntaxRegistry registry = registry();
        return new SyntaxRegistryHolder(registry, new ExpressionParser(registry));
    }

    public static ExpressionParser parser() {
        return holder().parser();
    }

    /** The event a header such as {@code on chat} parses to, with the values and cancelling it declares. */
    public static Event event(String header) {
        SyntaxRegistryHolder holder = holder();
        return holder.registry().matchFirst(holder.registry().events(), Tokenizer.tokenize(header), holder.parser(), scope(null))
                .orElseThrow(() -> new AssertionError("did not parse event: " + header));
    }

    public static ParseScope scope(Event event) {
        return new ParseScope("t.ms", 1, event);
    }

    public static Context context(FakeGameBridge game, Map<String, Object> values) {
        return new Context(game, "t.ms", values);
    }

    public static Expression expr(String text, SkType type, Event event) {
        return parser().parse(text, type, scope(event)).orElseThrow(() -> new AssertionError("did not parse: " + text));
    }

    public static Object eval(String text, SkType type, FakeGameBridge game, Event event) {
        return expr(text, type, event).evaluate(context(game, Map.of()));
    }

    public static Object eval(String text, SkType type, FakeGameBridge game) {
        return eval(text, type, game, new Event.Load());
    }

    public static Condition condition(String text, Event event) {
        SyntaxRegistryHolder holder = holder();
        return holder.registry().matchFirst(holder.registry().conditions(), Tokenizer.tokenize(text), holder.parser(), scope(event))
                .orElseThrow(() -> new AssertionError("did not parse condition: " + text));
    }

    public static Statement effect(String text, Event event) {
        SyntaxRegistryHolder holder = holder();
        return holder.registry().matchFirst(holder.registry().effects(), Tokenizer.tokenize(text), holder.parser(), scope(event))
                .orElseThrow(() -> new AssertionError("did not parse effect: " + text));
    }
}
