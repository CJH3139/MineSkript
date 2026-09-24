package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineskript.ScriptRunner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ListVariablePersistenceTest {
    private static VariablePersistence persistence(Path dir, ScriptRunner runner) {
        return new VariablePersistence(new VariableStore(), dir.resolve("variables.json"), runner.variables, runner.game);
    }

    @Test
    void globalListsSurviveASaveAndReload(@TempDir Path dir) {
        ScriptRunner first = new ScriptRunner();
        VariablePersistence saving = persistence(dir, first);
        saving.load();
        first.run("""
                on load:
                    set {homes::*} to 64, 70 and 12
                    set {homes::%player%} to 100
                    remove 70 from {homes::*}
                    set {-session::*} to "not saved"
                    set {plain} to "still here"
                """);
        assertTrue(saving.save());

        ScriptRunner second = new ScriptRunner();
        persistence(dir, second).load();
        second.run("""
                on load:
                    send {homes::*}
                    loop {homes::*}:
                        send "%loop-index%=%loop-value%"
                    send size of {-session::*}
                    send {plain}
                """);
        assertEquals(List.of("64, 12 and 100", "1=64", "3=12", "steve=100", "0", "still here"), second.game.messages);
    }

    @Test
    void entriesAreStoredAsFlatKeysNextToPlainVariables(@TempDir Path dir) throws IOException {
        ScriptRunner runner = new ScriptRunner();
        VariablePersistence saving = persistence(dir, runner);
        saving.load();
        runner.run("on load:\n    set {count} to 5\n    add \"a\" to {names::*}\n");
        assertTrue(saving.save());
        JsonObject root = JsonParser.parseString(Files.readString(dir.resolve("variables.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(List.of("count", "names::1"), List.copyOf(root.keySet()));
        assertEquals("a", root.getAsJsonObject("names::1").get("value").getAsString());
    }

    @Test
    void deletingAListIsSavedToo(@TempDir Path dir) {
        ScriptRunner runner = new ScriptRunner();
        VariablePersistence saving = persistence(dir, runner);
        saving.load();
        runner.run("on load:\n    set {l::*} to 1 and 2\n");
        assertTrue(saving.save());
        runner.run("on load:\n    delete {l::*}\n");
        assertTrue(saving.dirty());
        assertTrue(saving.save());
        ScriptRunner reloaded = new ScriptRunner();
        persistence(dir, reloaded).load();
        assertTrue(reloaded.variables.global().isEmpty());
    }

    @Test
    void anOldFileLoadsUnchangedAndOldColonNamesBecomeListEntries(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("variables.json"), """
                {
                  "count": {"type": "number", "value": 5.0},
                  "old list": {"type": "list", "value": [{"type": "text", "value": "a"}, {"type": "text", "value": "b"}]},
                  "warps::spawn": {"type": "text", "value": "0 64 0"}
                }
                """, StandardCharsets.UTF_8);
        ScriptRunner runner = new ScriptRunner();
        VariablePersistence loading = persistence(dir, runner);
        assertTrue(loading.load());
        runner.run("""
                on load:
                    send {count}
                    send {old list}
                    send size of {old list}
                    loop {warps::*}:
                        send "%loop-index%: %loop-value%"
                """);
        assertEquals(List.of("5", "a and b", "2", "spawn: 0 64 0"), runner.game.messages);
        assertFalse(loading.dirty());
    }
}
