package com.mineskript.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mineskript.api.example.ExampleAddon;
import com.mineskript.doc.JSONGenerator;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameSignals;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Execution;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.script.EventDispatcher;
import com.mineskript.script.ScriptRegistry;
import com.mineskript.syntax.DefaultSyntax;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Loading addons into the shared registry, without Fabric: order, failures, and addon syntax at work. */
class AddonLoaderTest {
    /** Adds one effect that shows the addon's name. */
    private record Named(String name, String pattern) implements MineSkriptAddon {
        @Override
        public void register(SyntaxRegistry registry) {
            registry.addEffect((match, scope) -> Optional.of(new Marker(scope.line(), name)), pattern);
        }
    }

    /** Would add an effect, but turns itself off. */
    private record Off(String name) implements MineSkriptAddon {
        @Override
        public boolean canLoad() {
            return false;
        }

        @Override
        public void register(SyntaxRegistry registry) {
            registry.addEffect((match, scope) -> Optional.of(new Marker(scope.line(), name)), "off thing");
        }
    }

    /** Registers an effect, then throws, as a broken addon might. */
    private static final class Broken implements MineSkriptAddon {
        @Override
        public String name() {
            return "Broken";
        }

        @Override
        public void register(SyntaxRegistry registry) {
            registry.addEffect((match, scope) -> Optional.of(new Marker(scope.line(), "broken")), "break things");
            throw new IllegalStateException("addon bug");
        }
    }

    private record Marker(int line, String text) implements Statement {
        @Override
        public Flow execute(Context context) {
            context.game().showMessage(text);
            return Flow.CONTINUE;
        }
    }

    private final FakeGameBridge game = new FakeGameBridge();

    private static List<String> errors(SyntaxRegistry registry, String source) {
        return new Parser(registry).parse("t.ms", source).errors().stream().map(ParseError::toString).toList();
    }

    private void run(SyntaxRegistry registry, String source) {
        ParsedScript script = new Parser(registry).parse("t.ms", source);
        assertEquals(List.of(), script.errors().stream().map(ParseError::toString).toList());
        for (Trigger trigger : script.triggers()) {
            new Interpreter(10_000).run(new Execution(trigger, new Context(game, "t.ms", Map.of())));
        }
    }

    @Test
    void addonsAreLoadedInOrderAfterTheBuiltInSyntax() {
        AddonLoader.Result result = AddonLoader.load(DefaultSyntax::registry,
                List.of(new Named("First", "first thing"), new Named("Second", "second thing")));
        assertEquals(List.of("First", "Second"), result.addons());
        assertEquals(List.of(), result.failures());
        run(result.registry(), "on load:\n    first thing\n    second thing\n    send \"built in\"\n");
        assertEquals(List.of("First", "Second", "built in"), game.messages);
    }

    @Test
    void noAddonsGiveJustTheBuiltInSyntax() {
        AddonLoader.Result result = AddonLoader.load(DefaultSyntax::registry, List.of());
        assertEquals(List.of(), result.addons());
        assertEquals(DefaultSyntax.registry().effects().size(), result.registry().effects().size());
    }

    @Test
    void aFailingAddonIsSkippedWithNothingItRegisteredLeftBehind() {
        List<Integer> built = new ArrayList<>();
        AddonLoader.Result result = AddonLoader.load(() -> {
            built.add(1);
            return DefaultSyntax.registry();
        }, List.of(new Named("First", "first thing"), new Broken(), new Named("Second", "second thing")));
        assertEquals(List.of("First", "Second"), result.addons());
        assertEquals(1, result.failures().size());
        assertEquals("Broken", result.failures().get(0).addon());
        assertInstanceOf(IllegalStateException.class, result.failures().get(0).error());
        assertEquals(2, built.size());
        assertEquals(List.of("t.ms:2: unknown effect \"break things\""), errors(result.registry(), "on load:\n    break things\n"));
        run(result.registry(), "on load:\n    first thing\n    second thing\n");
        assertEquals(List.of("First", "Second"), game.messages);
    }

    @Test
    void anAddonThatFailsToLinkIsSkippedToo() {
        MineSkriptAddon missingClass = new MineSkriptAddon() {
            @Override
            public String name() {
                return "Missing";
            }

            @Override
            public void register(SyntaxRegistry registry) {
                throw new NoClassDefFoundError("com/example/Gone");
            }
        };
        AddonLoader.Result result = AddonLoader.load(DefaultSyntax::registry, List.of(missingClass));
        assertEquals(List.of(), result.addons());
        assertEquals("Missing", result.failures().get(0).addon());
    }

    @Test
    void anAddonWithoutAUsableNameIsNamedAfterItsClass() {
        MineSkriptAddon nameless = new MineSkriptAddon() {
            @Override
            public String name() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void register(SyntaxRegistry registry) {
            }
        };
        assertEquals(nameless.getClass().getName(), AddonLoader.nameOf(nameless));
        assertEquals(Named.class.getName(), AddonLoader.nameOf(new Named(" ", "x")));
        assertEquals("Trimmed", AddonLoader.nameOf(new Named(" Trimmed ", "x")));
    }

    @Test
    void builtInPatternsWinOverAddonPatterns() {
        AddonLoader.Result result = AddonLoader.load(DefaultSyntax::registry, List.of(new Named("Sneaky", "send %string%")));
        run(result.registry(), "on load:\n    send \"hello\"\n");
        assertEquals(List.of("hello"), game.messages);
    }

    @Test
    void exampleAddonSyntaxParsesAndRuns() {
        SyntaxRegistry registry = AddonLoader.load(DefaultSyntax::registry, List.of(new ExampleAddon())).registry();
        run(registry, "on load:\n    shout \"the answer is %the answer to everything%\"\n"
                + "    if the answer to everything is 42:\n        shout \"right\"\n");
        assertEquals(List.of("THE ANSWER IS 42", "RIGHT"), game.messages);
    }

    @Test
    void exampleAddonEventFiresFromASignal() {
        SyntaxRegistry syntax = AddonLoader.load(DefaultSyntax::registry, List.of(new ExampleAddon())).registry();
        ScriptRegistry scripts = new ScriptRegistry();
        EventDispatcher dispatcher = new EventDispatcher(scripts, game, new Interpreter(10_000), new Scheduler());
        ParsedScript script = new Parser(syntax).parse("t.ms", "on ping:\n    shout \"ping %event-ping count%\"\n");
        assertEquals(List.of(), script.errors().stream().map(ParseError::toString).toList());
        scripts.replace(List.of(script));
        dispatcher.onSignal(new GameSignals.Signal("example:ping", Map.of("ping count", 3.0)));
        assertEquals(List.of("PING 3"), game.messages);
    }

    @Test
    void theDocumentationMarksWhichAddonEachElementCameFrom() {
        JsonObject docs = JSONGenerator.generate("test", List.of(new ExampleAddon()));
        assertEquals("Example Addon", find(docs, "effects", "Shout").get("addon").getAsString());
        assertEquals("Example Addon", find(docs, "expressions", "The Answer").get("addon").getAsString());
        assertEquals("Example Addon", find(docs, "events", "Ping").get("addon").getAsString());
        assertEquals(SyntaxRegistry.BUILT_IN, find(docs, "effects", "Send Message").get("addon").getAsString());
        assertEquals(SyntaxRegistry.BUILT_IN, find(docs, "events", "Chat").get("addon").getAsString());
        JsonObject builtIn = JSONGenerator.generate("test");
        for (String section : List.of("events", "conditions", "effects", "expressions")) {
            for (JsonElement element : builtIn.getAsJsonArray(section)) {
                assertEquals(SyntaxRegistry.BUILT_IN, element.getAsJsonObject().get("addon").getAsString());
            }
        }
    }

    @Test
    void anAddonsChildModulesLoadUnderItsName() {
        MineSkriptAddon parent = new MineSkriptAddon() {
            @Override
            public String name() {
                return "Parent";
            }

            @Override
            public void register(SyntaxRegistry registry) {
            }

            @Override
            public List<SyntaxModule> children() {
                return List.of(new ExampleAddon(), new Off("off"));
            }
        };
        SyntaxRegistry documenting = AddonLoader.load(SyntaxRegistry::documenting, List.of(parent)).registry();
        List<String> modules = documenting.registrations().stream().map(SyntaxRegistry.Registration::module)
                .distinct().toList();
        assertEquals(List.of("Parent/Example Addon", "Parent"), modules);
        assertEquals(1, documenting.effects().size());
    }

    @Test
    void anAddonThatCannotLoadIsLeftOutWithoutFailing() {
        AddonLoader.Result result = AddonLoader.load(DefaultSyntax::registry,
                List.of(new Off("Off"), new Named("On", "on thing")));
        assertEquals(List.of("On"), result.addons());
        assertEquals(List.of(), result.failures());
        assertEquals(List.of("t.ms:2: unknown effect \"off thing\""),
                errors(result.registry(), "on load:\n    off thing\n"));
    }

    @Test
    void registerAsGoesBackToBuiltInAfterAFailure() {
        SyntaxRegistry registry = new SyntaxRegistry();
        assertThrows(IllegalStateException.class, () -> registry.registerAs("X", ignored -> {
            throw new IllegalStateException();
        }));
        assertEquals(SyntaxRegistry.BUILT_IN, registry.addon());
    }

    private static JsonObject find(JsonObject docs, String section, String name) {
        for (JsonElement element : docs.getAsJsonArray(section)) {
            if (element.getAsJsonObject().get("name").getAsString().equals(name)) {
                return element.getAsJsonObject();
            }
        }
        throw new AssertionError("no " + name + " in " + section);
    }
}
