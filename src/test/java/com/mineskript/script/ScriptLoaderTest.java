package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.parse.Parser;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptLoaderTest {
    private final ScriptLoader loader = new ScriptLoader(new Parser(DefaultSyntax.registry()));

    @Test
    void createsTheFolderAndExampleWhenMissing(@TempDir Path root) {
        Path dir = root.resolve("mineskript");
        LoadReport report = loader.load(dir);
        assertTrue(Files.exists(dir.resolve("example.ms")));
        assertEquals(1, report.scriptCount());
        assertEquals(2, report.triggerCount());
        assertEquals(List.of(), report.errors());
        assertEquals("Loaded 1 script, 2 triggers, 0 errors", report.summary());
    }

    @Test
    void loadsEveryMsFileInNameOrderAndSkipsOtherFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("b.ms"), "on load:\n    send \"b\"\n");
        Files.writeString(dir.resolve("a.ms"), "on load:\n    send \"a\"\non chat:\n    stop\n");
        Files.writeString(dir.resolve("notes.txt"), "on load:\n    stop\n");
        LoadReport report = loader.load(dir);
        assertEquals(List.of("a.ms", "b.ms"), report.scripts().stream().map(script -> script.file()).toList());
        assertEquals(3, report.triggerCount());
        assertEquals("Loaded 2 scripts, 3 triggers, 0 errors", report.summary());
    }

    @Test
    void reportsParseErrorsPerFile(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("bad.ms"), "on load:\n    fly\n");
        LoadReport report = loader.load(dir);
        assertEquals(List.of("bad.ms:2: unknown effect \"fly\""), report.errors().stream().map(Object::toString).toList());
        assertEquals("Loaded 1 script, 0 triggers, 1 error", report.summary());
    }

    @Test
    void anExistingEmptyFolderStillGetsTheExample(@TempDir Path dir) {
        LoadReport report = loader.load(dir);
        assertTrue(Files.exists(dir.resolve("example.ms")));
        assertEquals(1, report.scriptCount());
    }
}
