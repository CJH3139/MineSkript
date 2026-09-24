package com.mineskript.scripttest;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.syntax.DefaultSyntax;
import java.util.Optional;

/**
 * The syntax script tests use: every built-in element, then the test event and {@link EffAssert}. It is only ever
 * registered here, so the shipped mod and the generated documentation never see it.
 */
public final class TestSyntax {
    /** The event name prefix of a test trigger; the rest of the name is what the test is called. */
    static final String EVENT_PREFIX = "script test:";

    private TestSyntax() {
    }

    public static SyntaxRegistry registry(AssertionLog log) {
        SyntaxRegistry registry = new SyntaxRegistry();
        DefaultSyntax.registerAll(registry);
        register(registry, log);
        return registry;
    }

    static void register(SyntaxRegistry registry, AssertionLog log) {
        registry.addEvent("Test", (match, scope) -> Optional.of(new Event.State(EVENT_PREFIX + name(match.slot(0)))),
                        "test %string%")
                .description("A script test. The test runner starts it once, on its own, with fresh variables.")
                .examples("test \"adding\":", "\tassert 1 + 1 is 2 with \"1 + 1 was not 2\"")
                .since("1.0.0-alpha.8");
        registry.addEvent("Unnamed Test", (match, scope) -> Optional.of(new Event.State(EVENT_PREFIX)), "test")
                .description("A script test named after its line.")
                .examples("test:", "\tassert 1 + 1 is 2 with \"1 + 1 was not 2\"")
                .since("1.0.0-alpha.8");
        EffAssert.register(registry, log);
    }

    private static String name(Expression expression) {
        if (!(expression instanceof ConstantExpression constant) || !(constant.value() instanceof String name)) {
            throw new SyntaxException("a test name must be plain text like \"adding\"");
        }
        return name;
    }

    /** Whether the event is a test, and so a trigger the runner should start. */
    static boolean isTest(Event event) {
        return event instanceof Event.State state && state.name().startsWith(EVENT_PREFIX);
    }

    static String testName(Event event) {
        return ((Event.State) event).name().substring(EVENT_PREFIX.length());
    }
}
