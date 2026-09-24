package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Enchantment;
import com.mineskript.lang.ast.EnchantmentType;
import com.mineskript.lang.ast.EntityType;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.GameMode;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.PotionEffectType;
import com.mineskript.lang.ast.Timespan;
import com.mineskript.lang.ast.WeatherType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VariableStoreTest {
    private final VariableStore store = new VariableStore();

    @Test
    void roundTripsEveryType(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("variables.json");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("text", "hello");
        values.put("number", 2.5);
        values.put("whole", 5.0);
        values.put("flag", true);
        values.put("delay", new Timespan(20));
        values.put("target", new BlockType("minecraft:stone"));
        values.put("names", List.of("a", 1.0, new BlockType("minecraft:dirt")));
        store.save(file, values);
        VariableStore.Loaded loaded = store.load(file);
        assertNull(loaded.warning());
        assertEquals(values, loaded.values());
        String text = Files.readString(file);
        assertTrue(text.contains("\"type\": \"number\""));
        assertTrue(text.contains("\"type\": \"timespan\""));
        assertTrue(text.contains("\n"));
    }

    @Test
    void roundTripsTheSkriptTypes(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("variables.json");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("mode", GameMode.SPECTATOR);
        values.put("weather", WeatherType.THUNDER);
        values.put("effect", new PotionEffectType("minecraft:night_vision"));
        values.put("enchantment", new Enchantment("minecraft:mending"));
        values.put("enchantment type", new EnchantmentType(new Enchantment("minecraft:sharpness"), 5));
        values.put("any level", new EnchantmentType(new Enchantment("mymod:frost"), EnchantmentType.ANY_LEVEL));
        values.put("entity type", new EntityType("minecraft:zombie"));
        store.save(file, values);
        VariableStore.Loaded loaded = store.load(file);
        assertNull(loaded.warning());
        assertEquals(values, loaded.values());
    }

    @Test
    void missingFileLoadsEmptyWithoutWarning(@TempDir Path dir) {
        VariableStore.Loaded loaded = store.load(dir.resolve("variables.json"));
        assertNull(loaded.warning());
        assertTrue(loaded.values().isEmpty());
    }

    @Test
    void saveLeavesNoTempFileAndCreatesParentFolders(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("nested").resolve("variables.json");
        store.save(file, Map.of("x", 1.0));
        assertTrue(Files.exists(file));
        try (Stream<Path> entries = Files.list(file.getParent())) {
            assertEquals(List.of(file), entries.toList());
        }
    }

    @Test
    void corruptFileIsRenamedAndReportedNotDeleted(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("variables.json");
        Files.writeString(file, "{ this is not json");
        VariableStore.Loaded loaded = store.load(file);
        assertTrue(loaded.values().isEmpty());
        assertNotNull(loaded.warning());
        assertTrue(loaded.warning().contains("variables.json.broken-"));
        assertFalse(Files.exists(file));
        try (Stream<Path> entries = Files.list(dir)) {
            List<String> names = entries.map(path -> path.getFileName().toString()).toList();
            assertEquals(1, names.size());
            assertTrue(names.get(0).startsWith("variables.json.broken-"));
        }
    }

    @Test
    void unknownTypeTagCountsAsCorrupt(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("variables.json");
        Files.writeString(file, "{ \"x\": { \"type\": \"dragon\", \"value\": 1 } }");
        VariableStore.Loaded loaded = store.load(file);
        assertTrue(loaded.values().isEmpty());
        assertNotNull(loaded.warning());
    }

    @Test
    void roundTripsItemsAndEntities(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("variables.json");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("tool", new ItemValue("minecraft:diamond_pickaxe", "diamond pickaxe", 1, 12, 1561));
        values.put("mob", new EntityValue("minecraft:zombie", "Zombie", 1.5, 64.0, -3.5, 7.25));
        store.save(file, values);
        VariableStore.Loaded loaded = store.load(file);
        assertNull(loaded.warning());
        assertEquals(values, loaded.values());
    }
}
