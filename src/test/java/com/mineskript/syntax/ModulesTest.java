package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.client.ClientModule;
import com.mineskript.common.CommonModule;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.module.ModuleLoader;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.parse.ExpressionParser;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxEntry;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.parse.Tokenizer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ModulesTest {
    private static final List<String> PATHS = List.of("common", "client", "client/movement", "client/inventory",
            "client/chat", "client/hud", "client/world", "client/entity", "client/player", "client/server", "client/visuals");

    @Test
    void theModuleTreeLoads() {
        Map<String, SyntaxModule> tree = ModuleLoader.tree(DefaultSyntax.modules());
        assertEquals(PATHS, List.copyOf(tree.keySet()));
        for (SyntaxModule module : tree.values()) {
            assertTrue(module.canLoad(), module.name());
        }
        SyntaxRegistry registry = SyntaxRegistry.documenting();
        DefaultSyntax.registerAll(registry);
        Set<String> used = new LinkedHashSet<>();
        registry.registrations().forEach(registration -> used.add(registration.module()));
        assertEquals(PATHS.stream().filter(path -> !path.equals("client")).toList(), List.copyOf(used));
    }

    @Test
    void everyRegisteredElementBelongsToExactlyOneModule() {
        SyntaxRegistry registry = SyntaxRegistry.documenting();
        DefaultSyntax.registerAll(registry);
        Map<String, Set<String>> modules = new TreeMap<>();
        for (SyntaxRegistry.Registration registration : registry.registrations()) {
            String element = registration.kind().equals("event")
                    ? "event " + registration.event().name()
                    : registration.owner().getName();
            modules.computeIfAbsent(element, key -> new HashSet<>()).add(registration.module());
        }
        List<String> wrong = new ArrayList<>();
        modules.forEach((element, in) -> {
            if (in.size() != 1 || in.contains(null)) {
                wrong.add(element + " is in " + in);
            } else if (!element.startsWith("event ") && !element.startsWith(packageOf(in.iterator().next()))) {
                wrong.add(element + " is registered by module " + in.iterator().next());
            }
        });
        assertEquals(List.of(), wrong);
    }

    @Test
    void everyElementClassIsRegistered() throws IOException {
        SyntaxRegistry registry = SyntaxRegistry.documenting();
        DefaultSyntax.registerAll(registry);
        Set<String> registered = new HashSet<>();
        registry.registrations().forEach(registration -> registered.add(registration.owner().getName()));
        Path root = Path.of("src/main/java");
        List<String> missing = new ArrayList<>();
        for (String module : List.of("common", "client")) {
            try (Stream<Path> files = Files.walk(root.resolve("com/mineskript").resolve(module))) {
                List<Path> elements = files
                        .filter(path -> path.getFileName().toString().matches("(Cond|Eff|Expr)[A-Z].*\\.java"))
                        .toList();
                for (Path file : elements) {
                    String name = root.relativize(file).toString().replace('\\', '/').replace('/', '.');
                    name = name.substring(0, name.length() - ".java".length());
                    if (!registered.contains(name)) {
                        missing.add(name);
                    }
                }
            }
        }
        assertEquals(List.of(), missing);
        assertTrue(registered.size() > 140);
    }

    @Test
    void entriesAreKeptInPriorityOrder() {
        SyntaxRegistry registry = DefaultSyntax.registry();
        assertSorted(registry.conditions());
        assertSorted(registry.effects());
        for (Tier tier : Tier.values()) {
            for (int i = 1; i < registry.expressions(tier).size(); i++) {
                assertTrue(registry.expressions(tier).get(i - 1).priority()
                        .compareTo(registry.expressions(tier).get(i).priority()) <= 0);
            }
        }
    }

    @Test
    void parsingDoesNotDependOnTheOrderTheClientModulesLoad() {
        SyntaxRegistry normal = DefaultSyntax.registry();
        List<SyntaxModule> children = new ArrayList<>(new ClientModule().children());
        Collections.reverse(children);
        SyntaxRegistry reversed = new SyntaxRegistry();
        ModuleLoader.load(reversed, List.of(new CommonModule(), new SyntaxModule() {
            @Override
            public String name() {
                return "client";
            }

            @Override
            public void register(SyntaxRegistry registry) {
            }

            @Override
            public List<SyntaxModule> children() {
                return children;
            }
        }));
        for (SyntaxRegistry registry : List.of(normal, reversed)) {
            assertEquals("EffSetRotation", effect(registry, "set yaw to 5"));
            assertEquals("VariableChange", effect(registry, "set {_x} to yaw"));
            assertEquals("EffStopScript", effect(registry, "stop script \"a\""));
            assertEquals("CondHasEffect", condition(registry, "player has effect speed"));
            assertEquals("CondHasItem", condition(registry, "player has stone"));
            assertEquals("CondIsSet", condition(registry, "{_x} is set"));
            assertEquals("CondPlayerState", condition(registry, "player is sneaking"));
            assertEquals("CondIsHolding", condition(registry, "player is holding stone"));
            assertEquals("Membership", condition(registry, "{_l::*} contains \"a\""));
            assertEquals("ExprItemInSlot", expression(registry, "item in slot {_s} parsed as number", SkType.ITEM));
            assertEquals("ExprInventoryCount", expression(registry, "number of stone in inventory", SkType.NUMBER));
            assertEquals("ExprListSize", expression(registry, "number of {_l::*}", SkType.NUMBER));
            assertEquals("ExprItemName", expression(registry, "name of item in slot 1's length", SkType.TEXT));
            assertEquals("ExprItemName", expression(registry, "name of {_x}", SkType.TEXT));
            assertEquals("ExprJoin", expression(registry, "level of effect \"a\" joined with \"b\"", SkType.TEXT));
            assertEquals("ExprLocationCoordinate", expression(registry, "x-coordinate of {_l}", SkType.NUMBER));
            assertEquals("ExprCoordinate", expression(registry, "x-coordinate of player", SkType.NUMBER));
            assertEquals("ExprEntityCoordinate", expression(registry, "target entity's y-coord", SkType.NUMBER));
            assertEquals("ExprBlock", expression(registry, "block at player", SkType.BLOCK));
            assertEquals("ExprBlockAt", expression(registry, "block at 2 above player", SkType.BLOCK));
            assertEquals("EffLookAt", effect(registry, "look at {_l}"));
            assertEquals("ExprParsedAsNumber", expression(registry, "block at {_l} parsed as number",
                    SkType.NUMBER));
        }
    }

    private static String packageOf(String module) {
        return "com.mineskript." + module.replace('/', '.') + ".elements.";
    }

    private static <T> void assertSorted(List<SyntaxEntry<T>> entries) {
        for (int i = 1; i < entries.size(); i++) {
            assertTrue(entries.get(i - 1).priority().compareTo(entries.get(i).priority()) <= 0);
        }
    }

    private static ParseScope scope() {
        return new ParseScope("t.ms", 1, new Event.Load());
    }

    private static String effect(SyntaxRegistry registry, String text) {
        return registry.matchFirst(registry.effects(), Tokenizer.tokenize(text), new ExpressionParser(registry),
                        scope())
                .map(statement -> statement.getClass().getSimpleName()).orElse("none");
    }

    private static String condition(SyntaxRegistry registry, String text) {
        return registry.matchFirst(registry.conditions(), Tokenizer.tokenize(text), new ExpressionParser(registry),
                scope()).map(parsed -> parsed.getClass().getSimpleName()).orElse("none");
    }

    private static String expression(SyntaxRegistry registry, String text, SkType type) {
        Expression expression = new ExpressionParser(registry).parse(text, type, scope()).orElseThrow();
        while (expression instanceof ConvertedExpression converted) {
            expression = converted.inner();
        }
        return expression.getClass().getSimpleName();
    }
}
