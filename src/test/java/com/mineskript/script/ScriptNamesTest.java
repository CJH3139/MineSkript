package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void aNameThatCannotBeTypedAsOneUnquotedWordIsNotSuggested(@TempDir Path dir) throws IOException {
        write(dir, "ok.ms");
        write(dir, "two words.ms");
        assertEquals(List.of("ok.ms", "two words.ms"), ScriptNames.list(dir));
        assertEquals(List.of("ok.ms"), ScriptNames.matching(dir, ""));
    }

    @Test
    void aMissingFolderIsAnEmptyList(@TempDir Path dir) {
        assertEquals(List.of(), ScriptNames.list(dir.resolve("nope")));
        assertEquals(List.of(), ScriptNames.matching(dir.resolve("nope"), "a"));
    }

    @Test
    void subfoldersAreNotListedAndOnlyPlainNamesAreEverReturned(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("nested.ms"));
        write(dir.resolve("nested.ms"), "inner.ms");
        write(dir, "top.ms");
        assertEquals(List.of("top.ms"), ScriptNames.list(dir));
        assertEquals(List.of("top.ms"), ScriptNames.matching(dir, ""));
    }

    @Test
    void aFileInPlaceOfTheFolderIsAnEmptyList(@TempDir Path dir) throws IOException {
        write(dir, "a.ms");
        assertEquals(List.of(), ScriptNames.list(dir.resolve("a.ms")));
        assertEquals(List.of(), ScriptNames.matching(dir.resolve("a.ms"), ""));
    }
}
