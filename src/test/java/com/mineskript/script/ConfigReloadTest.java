package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigReloadTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Variables variables = new Variables();
    private long nanos;
    private ScriptService service;

    private void start(Path root, SyntaxRegistry syntax) {
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), root.resolve("variables.json"), variables, game);
        EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), variables, () -> {
        });
        service = new ScriptService(root, new ScriptLoader(new Parser(syntax)), registry, dispatcher, persistence,
                new ConfigFile(root.resolve("config.txt")), () -> nanos += 1_000_000L);
        service.start();
    }

    private void start(Path root) {
        start(root, DefaultSyntax.registry());
    }

    @Test
    void startWritesTheTemplateAndReadsTheDefaults(@TempDir Path root) throws IOException {
        start(root);
        assertTrue(Files.isRegularFile(root.resolve("config.txt")));
        assertEquals(Config.TEMPLATE, Files.readString(root.resolve("config.txt"), StandardCharsets.UTF_8));
        assertEquals(Config.DEFAULTS, service.config().current());
    }

    @Test
    void startLeavesAnExistingConfigAlone(@TempDir Path root) throws IOException {
        Files.createDirectories(root);
        Files.writeString(root.resolve("config.txt"), "# mine\neffect command prefix: !\n", StandardCharsets.UTF_8);
        start(root);
        assertEquals("# mine\neffect command prefix: !\n", Files.readString(root.resolve("config.txt"), StandardCharsets.UTF_8));
        assertEquals("!", service.config().current().effectCommandPrefix());
    }

    @Test
    void reloadConfigRereadsTheFile(@TempDir Path root) throws IOException {
        start(root);
        assertEquals("?", service.config().current().effectCommandPrefix());
        Files.writeString(root.resolve("config.txt"), "effect command prefix: >>\n", StandardCharsets.UTF_8);
        assertEquals(ConfigReload.RELOADED, service.reloadConfig());
        assertEquals(">>", service.config().current().effectCommandPrefix());
        assertEquals(1L, service.lastMillis());
    }

    @Test
    void reloadConfigRewritesTheTemplateWhenTheFileIsGone(@TempDir Path root) throws IOException {
        start(root);
        Files.delete(root.resolve("config.txt"));
        assertEquals(ConfigReload.RELOADED, service.reloadConfig());
        assertEquals(Config.TEMPLATE, Files.readString(root.resolve("config.txt"), StandardCharsets.UTF_8));
    }

    @Test
    void reloadConfigCarriesTheWarningsOfABadFile(@TempDir Path root) throws IOException {
        start(root);
        Files.writeString(root.resolve("config.txt"), "fly speed: 9\n", StandardCharsets.UTF_8);
        assertEquals(ConfigReload.RELOADED, service.reloadConfig());
        assertEquals(List.of("config.txt line 1 is not a setting MineSkript knows, ignored: fly speed: 9"),
                service.config().takeWarnings());
    }

    @Test
    void reloadAllRereadsTheConfigToo(@TempDir Path root) throws IOException {
        start(root);
        Files.writeString(root.resolve("config.txt"), "effect commands: false\n", StandardCharsets.UTF_8);
        service.reloadAll();
        assertEquals(false, service.config().current().effectCommands());
    }

    @Test
    void reloadConfigSitsBehindTheSameReEntryGuardAsEveryOtherReload(@TempDir Path root) throws IOException {
        List<String> seen = new ArrayList<>();
        SyntaxRegistry syntax = new SyntaxRegistry();
        syntax.addEffect((match, scope) -> Optional.<Statement>of(new Statement() {
            @Override
            public int line() {
                return scope.line();
            }

            @Override
            public Flow execute(Context context) {
                seen.add("config:" + service.reloadConfig());
                seen.add("full:" + service.reload());
                seen.add("variables:" + service.reloadVariables());
                return Flow.CONTINUE;
            }
        }), "reenter reload config");
        DefaultSyntax.registerAll(syntax);
        Files.writeString(root.resolve("a.ms"), "on script load:\n    reenter reload config\n", StandardCharsets.UTF_8);
        start(root, syntax);
        assertEquals(List.of("config:BUSY", "full:null", "variables:BUSY"), seen);
        assertEquals(1L, service.lastMillis());
    }

    @Test
    void reloadConfigIsNotGuardedWhenNothingElseIsRunning(@TempDir Path root) throws IOException {
        start(root);
        assertEquals(ConfigReload.RELOADED, service.reloadConfig());
        assertEquals(ConfigReload.RELOADED, service.reloadConfig());
    }

    @Test
    void theMessagesReadTheWayTheTreeSaysTheyDo(@TempDir Path root) {
        Path file = root.resolve("config.txt");
        assertEquals(List.of("INFO reloading config.txt"), text(Messages.startingConfig(file)));
        assertEquals(List.of("SUCCESS re-read config.txt (3 ms)"),
                text(Messages.configReloaded(file, ConfigReload.RELOADED, List.of(), 3)));
        assertEquals(List.of("SUCCESS re-read config.txt (3 ms)", "WARNING bad line"),
                text(Messages.configReloaded(file, ConfigReload.RELOADED, List.of("bad line"), 3)));
        assertEquals(List.of("ERROR did not re-read config.txt, it could not be read (3 ms)"),
                text(Messages.configReloaded(file, ConfigReload.UNREADABLE, List.of(), 3)));
        assertEquals(List.of("WARNING a reload is already running, ignored"),
                text(Messages.configReloaded(file, ConfigReload.BUSY, List.of("ignored"), 3)));
        assertEquals(List.of(), text(Messages.configWarnings(List.of())));
    }

    private static List<String> text(List<MessageLine> lines) {
        return lines.stream().map(line -> line.kind() + " " + line.text()).toList();
    }
}
