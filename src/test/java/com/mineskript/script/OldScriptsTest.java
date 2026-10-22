package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OldScriptsTest {
    private final ScriptLoader loader = new ScriptLoader(new Parser(DefaultSyntax.registry()));

    private static void write(Path dir, String name) throws IOException {
        Path file = dir.resolve(name);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "on load:\n    send \"hi\"\n", StandardCharsets.UTF_8);
    }

    @Test
    void oldScriptsAreFoundInFoldersButNotInDisabledOnes(@TempDir Path dir) throws IOException {
        write(dir, "b.ms");
        write(dir, "pvp/a.MS");
        write(dir, "-off/c.ms");
        write(dir, "new.sk");
        assertEquals(List.of("b.ms", "pvp/a.MS"), ScriptNames.oldScripts(dir));
        assertEquals(List.of(), ScriptNames.oldScripts(dir.resolve("missing")));
    }

    @Test
    void aLoadListsTheOldScriptsItSkipped(@TempDir Path dir) throws IOException {
        write(dir, "chat.ms");
        write(dir, "chat.sk");
        LoadReport report = loader.load(dir);
        assertEquals(List.of("chat.sk"), report.scripts().stream().map(script -> script.file()).toList());
        assertEquals(List.of("chat.ms"), report.oldScripts());
        assertEquals("1 script not loaded because .ms is no longer used, rename to .sk: chat.ms",
                report.oldScriptsWarning());
    }

    @Test
    void noOldScriptsMeansNoWarning(@TempDir Path dir) throws IOException {
        write(dir, "chat.sk");
        assertNull(loader.load(dir).oldScriptsWarning());
        assertEquals(List.of("SUCCESS loaded 1 script, 1 trigger (3 ms)"),
                Messages.reloaded(loader.load(dir), 3).stream().map(line -> line.kind() + " " + line.text()).toList());
    }

    @Test
    void theWarningNamesFiveAndCountsTheRest() {
        LoadReport report = new LoadReport(List.of(), List.of("a.ms", "b.ms", "c.ms", "d.ms", "e.ms", "f.ms", "g.ms"));
        assertEquals("7 scripts not loaded because .ms is no longer used, rename to .sk: a.ms, b.ms, c.ms, d.ms, e.ms and 2 more",
                report.oldScriptsWarning());
    }

    @Test
    void aReloadAddsTheWarningAfterItsSummary() {
        LoadReport report = new LoadReport(List.of(), List.of("old.ms"));
        assertEquals(List.of("SUCCESS loaded 0 scripts, 0 triggers (3 ms)",
                        "WARNING 1 script not loaded because .ms is no longer used, rename to .sk: old.ms"),
                Messages.reloaded(report, 3).stream().map(line -> line.kind() + " " + line.text()).toList());
    }

    @Test
    void startingWarnsOnceInChatWhenAWorldIsOpen(@TempDir Path dir) throws IOException {
        write(dir, "old.ms");
        FakeGameBridge game = new FakeGameBridge();
        Variables variables = new Variables();
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), dir.resolve("variables.json"), variables, game);
        EventDispatcher dispatcher = new EventDispatcher(new ScriptRegistry(), game, new Interpreter(10_000), new Scheduler(), variables, () -> {
        });
        ScriptService service = new ScriptService(dir, loader, new ScriptRegistry(), dispatcher, persistence, new ConfigFile(dir.resolve(Config.NAME)));
        service.start();
        game.hasWorld = false;
        service.flushOldScripts(game);
        assertEquals(List.of(), game.warnings);
        game.hasWorld = true;
        service.flushOldScripts(game);
        service.flushOldScripts(game);
        assertEquals(List.of("1 script not loaded because .ms is no longer used, rename to .sk: old.ms"), game.warnings);
    }
}
