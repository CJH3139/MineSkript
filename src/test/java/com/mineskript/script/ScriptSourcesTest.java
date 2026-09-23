package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptSourcesTest {
    @Test
    void readsTheSourceLineAndStripsTheIndent(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("a.ms"), "on load:\n    fly\n", StandardCharsets.UTF_8);
        ScriptSources sources = new ScriptSources(dir);
        assertEquals("on load:", sources.line("a.ms", 1));
        assertEquals("fly", sources.line("a.ms", 2));
    }

    @Test
    void anOutOfRangeLineAMissingFileAndAPathAreAllEmpty(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("a.ms"), "on load:\n", StandardCharsets.UTF_8);
        ScriptSources sources = new ScriptSources(dir);
        assertEquals("", sources.line("a.ms", 0));
        assertEquals("", sources.line("a.ms", 2));
        assertEquals("", sources.line("gone.ms", 1));
        assertEquals("", sources.line("sub/a.ms", 1));
        assertEquals("", sources.line(dir.toString(), 0));
    }

    @Test
    void aNameThatWalksOutOfTheScriptsFolderNeverReachesTheFilesystem(@TempDir Path dir) throws IOException {
        Path scripts = Files.createDirectories(dir.resolve("scripts"));
        Files.writeString(dir.resolve("secret.ms"), "secret line\n", StandardCharsets.UTF_8);
        Files.writeString(scripts.resolve("a.ms"), "on load:\n", StandardCharsets.UTF_8);
        ScriptSources sources = new ScriptSources(scripts);
        assertEquals("on load:", sources.line("a.ms", 1));
        assertEquals("", sources.line("../secret.ms", 1));
        assertEquals("", sources.line("..\\secret.ms", 1));
        assertEquals("", sources.line("sub/a.ms", 1));
        assertEquals("", sources.line("sub\\a.ms", 1));
        assertEquals("", sources.line(dir.resolve("secret.ms").toString(), 1));
        assertEquals("", sources.line("/etc/hosts", 1));
    }

    @Test
    void anEmptyNameAndAFolderNameAreEmpty(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("sub"));
        ScriptSources sources = new ScriptSources(dir);
        assertEquals("", sources.line("", 1));
        assertEquals("", sources.line("sub", 1));
    }
}
