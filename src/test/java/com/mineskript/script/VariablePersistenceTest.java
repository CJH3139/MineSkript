package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.VariableScope;
import com.mineskript.lang.runtime.Variables;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VariablePersistenceTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final Variables variables = new Variables();

    private VariablePersistence persistence(Path dir) {
        return new VariablePersistence(new VariableStore(), dir.resolve("variables.json"), variables, game);
    }

    @Test
    void loadReplacesGlobalsAndStartsClean(@TempDir Path dir) throws IOException {
        new VariableStore().save(dir.resolve("variables.json"), Map.of("count", 5.0));
        variables.global().put("stale", 1.0);
        VariablePersistence persistence = persistence(dir);
        persistence.load();
        assertEquals(Map.of("count", 5.0), variables.global());
        assertFalse(persistence.dirty());
        assertFalse(persistence.save());
        assertTrue(game.errors.isEmpty());
    }

    @Test
    void saveWritesOnlyWhenDirty(@TempDir Path dir) {
        VariablePersistence persistence = persistence(dir);
        persistence.load();
        assertFalse(persistence.save());
        assertFalse(Files.exists(dir.resolve("variables.json")));
        Context context = new Context(game, "t.ms", Map.of(), variables);
        context.setVariable(VariableScope.GLOBAL, "count", 1.0);
        assertTrue(persistence.dirty());
        assertTrue(persistence.save());
        assertTrue(Files.exists(dir.resolve("variables.json")));
        assertFalse(persistence.dirty());
        assertFalse(persistence.save());
        context.setVariable(VariableScope.RAM, "session", 1.0);
        assertFalse(persistence.dirty());
    }

    @Test
    void gameValuesAreNormalisedAndNoneIsSkipped(@TempDir Path dir) {
        game.name = "Alex";
        VariablePersistence persistence = persistence(dir);
        persistence.load();
        Context context = new Context(game, "t.ms", Map.of(), variables);
        context.setVariable(VariableScope.GLOBAL, "block", new BlockValue("minecraft:stone"));
        context.setVariable(VariableScope.GLOBAL, "who", PlayerRef.LOCAL);
        context.setVariable(VariableScope.GLOBAL, "gone", None.NONE);
        context.setVariable(VariableScope.GLOBAL, "mixed", List.of(new BlockValue("minecraft:dirt"), None.NONE, "x"));
        assertTrue(persistence.save());
        Variables reloaded = new Variables();
        new VariablePersistence(new VariableStore(), dir.resolve("variables.json"), reloaded, game).load();
        assertEquals(new BlockType("minecraft:stone"), reloaded.global().get("block"));
        assertEquals("Alex", reloaded.global().get("who"));
        assertFalse(reloaded.global().containsKey("gone"));
        assertEquals(List.of(new BlockType("minecraft:dirt"), "x"), reloaded.global().get("mixed"));
    }

    @Test
    void corruptFileWarningReachesTheGame(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("variables.json"), "nope");
        VariablePersistence persistence = persistence(dir);
        persistence.load();
        persistence.flushWarning();
        assertEquals(1, game.errors.size());
        assertTrue(game.errors.get(0).contains("variables.json.broken-"));
        assertTrue(variables.global().isEmpty());
    }

    @Test
    void theCorruptFileWarningWaitsForAWorld(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("variables.json"), "nope");
        game.hasWorld = false;
        VariablePersistence persistence = persistence(dir);
        persistence.load();
        assertTrue(game.errors.isEmpty());
        persistence.flushWarning();
        assertTrue(game.errors.isEmpty());
        game.hasWorld = true;
        persistence.flushWarning();
        assertEquals(1, game.errors.size());
        assertTrue(game.errors.get(0).contains("variables.json.broken-"));
        persistence.flushWarning();
        assertEquals(1, game.errors.size());
    }
}
