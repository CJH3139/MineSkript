package com.mineskript.lang.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.ExpressionParser;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tokenizer;
import com.mineskript.lang.runtime.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Loading module trees, and trying syntax by priority rather than by the module that registered it. */
class ModuleLoaderTest {
    /** A module that records when it registers, with the path the registry says it is in. */
    private static final class Recording implements SyntaxModule {
        private final String name;
        private final boolean loads;
        private final List<String> log;
        private final List<SyntaxModule> children;

        Recording(String name, boolean loads, List<String> log, SyntaxModule... children) {
            this.name = name;
            this.loads = loads;
            this.log = log;
            this.children = List.of(children);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean canLoad() {
            return loads;
        }

        @Override
        public void register(SyntaxRegistry registry) {
            log.add(registry.module().orElse("none"));
        }

        @Override
        public List<SyntaxModule> children() {
            return children;
        }
    }

    private record Marker(String name) implements Condition {
        @Override
        public boolean test(Context context) {
            return true;
        }
    }

    @Test
    void aModuleLoadsBeforeItsChildrenUnderItsPath() {
        List<String> log = new ArrayList<>();
        SyntaxModule tree = new Recording("top", true, log,
                new Recording("a", true, log, new Recording("deep", true, log)), new Recording("b", true, log));
        SyntaxRegistry registry = new SyntaxRegistry();
        ModuleLoader.load(registry, List.of(tree));
        assertEquals(List.of("top", "top/a", "top/a/deep", "top/b"), log);
        assertEquals(Optional.empty(), registry.module());
        assertEquals(log, List.copyOf(ModuleLoader.tree(List.of(tree)).keySet()));
    }

    @Test
    void aModuleThatCannotLoadIsSkippedWithItsChildren() {
        List<String> log = new ArrayList<>();
        SyntaxModule tree = new Recording("top", true, log,
                new Recording("off", false, log, new Recording("child", true, log)), new Recording("on", true, log));
        ModuleLoader.load(new SyntaxRegistry(), List.of(tree));
        assertEquals(List.of("top", "top/on"), log);
    }

    @Test
    void moduleNamesMustBeUsableInAPath() {
        List<String> log = new ArrayList<>();
        SyntaxRegistry registry = new SyntaxRegistry();
        assertThrows(IllegalArgumentException.class, () -> ModuleLoader.load(registry,
                List.of(new Recording("top", true, log, new Recording("x", true, log), new Recording("x", true, log)))));
        assertThrows(IllegalArgumentException.class, () -> ModuleLoader.load(registry,
                List.of(new Recording("a/b", true, log))));
        assertThrows(IllegalArgumentException.class, () -> ModuleLoader.load(registry,
                List.of(new Recording(" ", true, log))));
    }

    @Test
    void theModuleGoesBackAfterARegistrationThrows() {
        SyntaxRegistry registry = new SyntaxRegistry();
        assertThrows(IllegalStateException.class, () -> registry.registerIn("broken", ignored -> {
            throw new IllegalStateException();
        }));
        assertEquals(Optional.empty(), registry.module());
    }

    @Test
    void theDocumentationRecordsEachElementsModule() {
        SyntaxRegistry registry = SyntaxRegistry.documenting();
        ModuleLoader.load(registry, List.of(new SyntaxModule() {
            @Override
            public String name() {
                return "things";
            }

            @Override
            public void register(SyntaxRegistry target) {
                target.addCondition((match, scope) -> Optional.of(new Marker("thing")), "thing");
            }
        }));
        assertEquals("things", registry.registrations().get(0).module());
    }

    @Test
    void aCatchAllRegisteredFirstIsStillTriedLast() {
        SyntaxRegistry registry = new SyntaxRegistry();
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING, (match, scope) -> Optional.of(new Marker("any")),
                "%objects% is %objects%");
        registry.addCondition(Priority.COMBINED, (match, scope) -> Optional.of(new Marker("combined")),
                "%objects% is %objects% too");
        registry.addCondition((match, scope) -> Optional.of(new Marker("simple")), "%objects% is special");
        assertEquals("simple", condition(registry, "5 is special"));
        assertEquals("combined", condition(registry, "5 is 6 too"));
        assertEquals("any", condition(registry, "5 is 6"));
    }

    @Test
    void beforeAndAfterSitNextToTheirPriority() {
        assertTrue(Priority.before(Priority.SIMPLE).compareTo(Priority.SIMPLE) < 0);
        assertTrue(Priority.after(Priority.SIMPLE).compareTo(Priority.SIMPLE) > 0);
        assertTrue(Priority.after(Priority.SIMPLE).compareTo(Priority.COMBINED) < 0);
        assertTrue(Priority.before(Priority.COMBINED).compareTo(Priority.after(Priority.SIMPLE)) > 0);
        assertTrue(Priority.COMBINED.compareTo(Priority.PATTERN_MATCHES_EVERYTHING) < 0);
        assertEquals(Priority.SIMPLE, Priority.after(Priority.before(Priority.SIMPLE)));
    }

    private static String condition(SyntaxRegistry registry, String text) {
        ParseScope scope = new ParseScope("t.ms", 1, new Event.Load());
        return registry.matchFirst(registry.conditions(), Tokenizer.tokenize(text), new ExpressionParser(registry),
                        scope)
                .map(parsed -> ((Marker) parsed).name()).orElse("none");
    }
}
