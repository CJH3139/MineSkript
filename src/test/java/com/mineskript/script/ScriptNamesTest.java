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
    void listsOnlySkFilesInNameOrder(@TempDir Path dir) throws IOException {
        write(dir, "b.sk");
        write(dir, "a.sk");
        write(dir, "notes.txt");
        write(dir, "old.ms");
        assertEquals(List.of("a.sk", "b.sk"), ScriptNames.list(dir));
    }

    @Test
    void matchingIsCaseInsensitiveOnThePrefix(@TempDir Path dir) throws IOException {
        write(dir, "Chat.sk");
        write(dir, "combat.sk");
        write(dir, "mining.sk");
        assertEquals(List.of("Chat.sk", "combat.sk"), ScriptNames.matching(dir, "c"));
        assertEquals(List.of("Chat.sk"), ScriptNames.matching(dir, "CHA"));
        assertEquals(List.of("Chat.sk", "combat.sk", "mining.sk"), ScriptNames.matching(dir, ""));
        assertEquals(List.of(), ScriptNames.matching(dir, "z"));
    }

    @Test
    void namesWithSpacesAreSuggestedToo(@TempDir Path dir) throws IOException {
        write(dir, "ok.sk");
        write(dir, "two words.sk");
        assertEquals(List.of("ok.sk", "two words.sk"), ScriptNames.list(dir));
        assertEquals(List.of("ok.sk", "two words.sk"), ScriptNames.matching(dir, ""));
    }

    @Test
    void aMissingFolderIsAnEmptyList(@TempDir Path dir) {
        assertEquals(List.of(), ScriptNames.list(dir.resolve("nope")));
        assertEquals(List.of(), ScriptNames.matching(dir.resolve("nope"), "a"));
    }

    @Test
    void scriptsInFoldersAreListedByTheirPathWithForwardSlashes(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("pvp/kits"));
        write(dir.resolve("pvp"), "combat.sk");
        write(dir.resolve("pvp/kits"), "archer.sk");
        write(dir, "top.sk");
        assertEquals(List.of("pvp/combat.sk", "pvp/kits/archer.sk", "top.sk"), ScriptNames.list(dir));
        assertEquals(List.of("pvp/combat.sk", "pvp/kits/archer.sk"), ScriptNames.matching(dir, "pvp"));
        assertEquals(List.of("pvp/kits/archer.sk"), ScriptNames.matching(dir, "PVP\\K"));
        assertEquals(java.util.Optional.of("pvp/combat.sk"), ScriptNames.onDisk(dir, "pvp\\Combat.sk"));
    }

    @Test
    void aDashDisablesAScriptOrAWholeFolderLikeSkript(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("-old"));
        Files.createDirectories(dir.resolve("new"));
        write(dir.resolve("-old"), "a.sk");
        write(dir.resolve("new"), "-b.sk");
        write(dir.resolve("new"), "c.sk");
        write(dir, "-d.sk");
        assertEquals(List.of("new/c.sk"), ScriptNames.list(dir));
    }

    @Test
    void onlyNamesThatStayInsideTheFolderAreScriptNames(@TempDir Path dir) {
        assertTrue(ScriptNames.isScriptName("a.sk"));
        assertTrue(ScriptNames.isScriptName("pvp/combat.sk"));
        assertTrue(ScriptNames.isScriptName("pvp\\combat.sk"));
        assertFalse(ScriptNames.isScriptName("../a.sk"));
        assertFalse(ScriptNames.isScriptName("pvp/../../a.sk"));
        assertFalse(ScriptNames.isScriptName("/a.sk"));
        assertFalse(ScriptNames.isScriptName("C:/a.sk"));
        assertFalse(ScriptNames.isScriptName("pvp//a.sk"));
        assertFalse(ScriptNames.isScriptName("-old/a.sk"));
        assertFalse(ScriptNames.isScriptName("pvp/notes.txt"));
        assertFalse(ScriptNames.isScriptName("old.ms"));
        assertEquals(java.util.Optional.empty(), ScriptNames.resolve(dir, "../a.sk"));
        assertEquals(java.util.Optional.of(dir.toAbsolutePath().normalize().resolve("pvp/a.sk")),
                ScriptNames.resolve(dir, "pvp\\a.sk"));
    }

    @Test
    void aFileInPlaceOfTheFolderIsAnEmptyList(@TempDir Path dir) throws IOException {
        write(dir, "a.sk");
        assertEquals(List.of(), ScriptNames.list(dir.resolve("a.sk")));
        assertEquals(List.of(), ScriptNames.matching(dir.resolve("a.sk"), ""));
    }
}
