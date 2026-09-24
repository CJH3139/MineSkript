package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScriptNamesTest {
    private void write(Path dir, String name) throws IOException {
        Files.writeString(dir.resolve(name), "", StandardCharsets.UTF_8);
    }

    @Test
    void listsOnlyMsFilesInNameOrder(@TempDir Path dir) throws IOException {
        write(dir, "b.ms");
        write(dir, "a.ms");
        write(dir, "notes.txt");
        assertEquals(List.of("a.ms", "b.ms"), ScriptNames.list(dir));
    }

    @Test
    void matchingIsCaseInsensitiveOnThePrefix(@TempDir Path dir) throws IOException {
        write(dir, "Chat.ms");
        write(dir, "combat.ms");
        write(dir, "mining.ms");
        assertEquals(List.of("Chat.ms", "combat.ms"), ScriptNames.matching(dir, "c"));
        assertEquals(List.of("Chat.ms"), ScriptNames.matching(dir, "CHA"));
        assertEquals(List.of("Chat.ms", "combat.ms", "mining.ms"), ScriptNames.matching(dir, ""));
        assertEquals(List.of(), ScriptNames.matching(dir, "z"));
    }

    @Test
    void namesWithSpacesAreSuggestedToo(@TempDir Path dir) throws IOException {
        write(dir, "ok.ms");
        write(dir, "two words.ms");
        assertEquals(List.of("ok.ms", "two words.ms"), ScriptNames.list(dir));
        assertEquals(List.of("ok.ms", "two words.ms"), ScriptNames.matching(dir, ""));
    }

    @Test
    void aMissingFolderIsAnEmptyList(@TempDir Path dir) {
        assertEquals(List.of(), ScriptNames.list(dir.resolve("nope")));
        assertEquals(List.of(), ScriptNames.matching(dir.resolve("nope"), "a"));
    }

    @Test
    void scriptsInFoldersAreListedByTheirPathWithForwardSlashes(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("pvp/kits"));
        write(dir.resolve("pvp"), "combat.ms");
        write(dir.resolve("pvp/kits"), "archer.ms");
        write(dir, "top.ms");
        assertEquals(List.of("pvp/combat.ms", "pvp/kits/archer.ms", "top.ms"), ScriptNames.list(dir));
        assertEquals(List.of("pvp/combat.ms", "pvp/kits/archer.ms"), ScriptNames.matching(dir, "pvp"));
        assertEquals(List.of("pvp/kits/archer.ms"), ScriptNames.matching(dir, "PVP\\K"));
        assertEquals(java.util.Optional.of("pvp/combat.ms"), ScriptNames.onDisk(dir, "pvp\\Combat.ms"));
    }

    @Test
    void aDashDisablesAScriptOrAWholeFolderLikeSkript(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("-old"));
        Files.createDirectories(dir.resolve("new"));
        write(dir.resolve("-old"), "a.ms");
        write(dir.resolve("new"), "-b.ms");
        write(dir.resolve("new"), "c.ms");
        write(dir, "-d.ms");
        assertEquals(List.of("new/c.ms"), ScriptNames.list(dir));
    }

    @Test
    void onlyNamesThatStayInsideTheFolderAreScriptNames(@TempDir Path dir) {
        assertTrue(ScriptNames.isScriptName("a.ms"));
        assertTrue(ScriptNames.isScriptName("pvp/combat.ms"));
        assertTrue(ScriptNames.isScriptName("pvp\\combat.ms"));
        assertFalse(ScriptNames.isScriptName("../a.ms"));
        assertFalse(ScriptNames.isScriptName("pvp/../../a.ms"));
        assertFalse(ScriptNames.isScriptName("/a.ms"));
        assertFalse(ScriptNames.isScriptName("C:/a.ms"));
        assertFalse(ScriptNames.isScriptName("pvp//a.ms"));
        assertFalse(ScriptNames.isScriptName("-old/a.ms"));
        assertFalse(ScriptNames.isScriptName("pvp/notes.txt"));
        assertEquals(java.util.Optional.empty(), ScriptNames.resolve(dir, "../a.ms"));
        assertEquals(java.util.Optional.of(dir.toAbsolutePath().normalize().resolve("pvp/a.ms")),
                ScriptNames.resolve(dir, "pvp\\a.ms"));
    }

    @Test
    void aFileInPlaceOfTheFolderIsAnEmptyList(@TempDir Path dir) throws IOException {
        write(dir, "a.ms");
        assertEquals(List.of(), ScriptNames.list(dir.resolve("a.ms")));
        assertEquals(List.of(), ScriptNames.matching(dir.resolve("a.ms"), ""));
    }
}
