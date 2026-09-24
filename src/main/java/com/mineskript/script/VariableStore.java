package com.mineskript.script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineskript.lang.Language;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.Timespan;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VariableStore {
    public record Loaded(Map<String, Object> values, String warning) {
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public Loaded load(Path file) {
        if (!Files.exists(file)) {
            return new Loaded(Map.of(), null);
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, Object> values = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                values.put(entry.getKey(), decode(entry.getValue().getAsJsonObject()));
            }
            return new Loaded(values, null);
        } catch (IOException | RuntimeException error) {
            Path broken = file.resolveSibling(file.getFileName() + ".broken-" + System.currentTimeMillis());
            try {
                Files.move(file, broken, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                return new Loaded(Map.of(), Language.format("variables.unreadable", file.getFileName(), error.getMessage()));
            }
            return new Loaded(Map.of(), Language.format("variables.unreadable-moved",
                    file.getFileName(), broken.getFileName(), error.getMessage()));
        }
    }

    public void save(Path file, Map<String, Object> values) throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            root.add(entry.getKey(), encode(entry.getValue()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, gson.toJson(root), StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private JsonObject encode(Object value) {
        JsonObject object = new JsonObject();
        switch (value) {
            case String text -> {
                object.addProperty("type", "text");
                object.addProperty("value", text);
            }
            case Double number -> {
                object.addProperty("type", "number");
                object.addProperty("value", number);
            }
            case Boolean flag -> {
                object.addProperty("type", "boolean");
                object.addProperty("value", flag);
            }
            case Timespan timespan -> {
                object.addProperty("type", "timespan");
                object.addProperty("value", timespan.ticks());
            }
            case BlockType type -> {
                object.addProperty("type", "blocktype");
                object.addProperty("value", type.id());
            }
            case List<?> list -> {
                JsonArray items = new JsonArray();
                for (Object item : list) {
                    items.add(encode(item));
                }
                object.addProperty("type", "list");
                object.add("value", items);
            }
            case ItemValue item -> {
                object.addProperty("type", "item");
                JsonObject fields = new JsonObject();
                fields.addProperty("id", item.id());
                fields.addProperty("name", item.name());
                fields.addProperty("count", item.count());
                fields.addProperty("damage", item.damage());
                fields.addProperty("maxDamage", item.maxDamage());
                object.add("value", fields);
            }
            case EntityValue entity -> {
                object.addProperty("type", "entity");
                JsonObject fields = new JsonObject();
                fields.addProperty("id", entity.id());
                fields.addProperty("name", entity.name());
                fields.addProperty("x", entity.x());
                fields.addProperty("y", entity.y());
                fields.addProperty("z", entity.z());
                fields.addProperty("distance", entity.distance());
                object.add("value", fields);
            }
            default -> throw new IllegalArgumentException("cannot save a " + value.getClass().getSimpleName());
        }
        return object;
    }

    private Object decode(JsonObject object) {
        String type = object.get("type").getAsString();
        JsonElement value = object.get("value");
        return switch (type) {
            case "text" -> value.getAsString();
            case "number" -> value.getAsDouble();
            case "boolean" -> value.getAsBoolean();
            case "timespan" -> new Timespan(value.getAsInt());
            case "blocktype" -> new BlockType(value.getAsString());
            case "list" -> {
                List<Object> items = new ArrayList<>();
                for (JsonElement item : value.getAsJsonArray()) {
                    items.add(decode(item.getAsJsonObject()));
                }
                yield List.copyOf(items);
            }
            case "item" -> {
                JsonObject fields = value.getAsJsonObject();
                yield new ItemValue(fields.get("id").getAsString(), fields.get("name").getAsString(),
                        fields.get("count").getAsInt(), fields.get("damage").getAsInt(), fields.get("maxDamage").getAsInt());
            }
            case "entity" -> {
                JsonObject fields = value.getAsJsonObject();
                yield new EntityValue(fields.get("id").getAsString(), fields.get("name").getAsString(),
                        fields.get("x").getAsDouble(), fields.get("y").getAsDouble(),
                        fields.get("z").getAsDouble(), fields.get("distance").getAsDouble());
            }
            default -> throw new IllegalArgumentException("unknown variable type " + type);
        };
    }
}
