package com.mineskript.doc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class JSONGeneratorTest {
    private final JsonObject docs = JSONGenerator.generate("test");

    private JsonObject event(String name) {
        for (JsonElement element : docs.getAsJsonArray("events")) {
            if (element.getAsJsonObject().get("name").getAsString().equals(name)) {
                return element.getAsJsonObject();
            }
        }
        throw new AssertionError("no event " + name);
    }

    private static List<String> strings(JsonArray array, String field) {
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            values.add(field == null ? element.getAsString() : element.getAsJsonObject().get(field).getAsString());
        }
        return values;
    }

    @Test
    void eventsListTheirDeclaredValues() {
        JsonObject damage = event("Damage");
        assertEquals(List.of("event-damage", "event-old health"),
                strings(damage.getAsJsonArray("eventValues"), "name"));
        JsonObject value = damage.getAsJsonArray("eventValues").get(0).getAsJsonObject();
        assertEquals("number", value.get("type").getAsString());
        assertTrue(value.get("description").getAsString().startsWith("Health lost in this hit"));

        assertEquals(List.of("event-durability", "event-item"),
                strings(event("Durability Below").getAsJsonArray("eventValues"), "name"));
        assertEquals(List.of("event-block", "event-block x", "event-block y", "event-block z", "event-location"),
                strings(event("Block Break").getAsJsonArray("eventValues"), "name"));
        assertEquals(List.of("item type", "number", "number", "number", "location"),
                strings(event("Block Break").getAsJsonArray("eventValues"), "type"));
        assertEquals(List.of(), strings(event("Jump").getAsJsonArray("eventValues"), "name"));
    }

    @Test
    void chatEventsListTheMessage() {
        for (String name : List.of("Chat", "Chat Send", "Command Send")) {
            JsonArray values = event(name).getAsJsonArray("eventValues");
            assertEquals(List.of("message"), strings(values, "name"), name);
            assertEquals("The chat message, or the command without its leading slash.",
                    values.get(0).getAsJsonObject().get("description").getAsString());
        }
    }

    @Test
    void onlyChatSendAndCommandSendAreCancellable() {
        List<String> cancellable = new ArrayList<>();
        for (JsonElement element : docs.getAsJsonArray("events")) {
            JsonObject event = element.getAsJsonObject();
            if (event.get("cancellable").getAsBoolean()) {
                cancellable.add(event.get("name").getAsString());
            }
        }
        assertEquals(List.of("Chat Send", "Command Send"), cancellable);
        assertFalse(event("Chat").get("cancellable").getAsBoolean());
    }

    @Test
    void onlyItemTooltipIsInstant() {
        List<String> instant = new ArrayList<>();
        for (JsonElement element : docs.getAsJsonArray("events")) {
            JsonObject event = element.getAsJsonObject();
            if (event.get("instant").getAsBoolean()) {
                instant.add(event.get("name").getAsString());
            }
        }
        assertEquals(List.of("Item Tooltip"), instant);
    }

    @Test
    void theTopLevelValuesListTheEventsThatProvideThem() {
        JsonObject item = null;
        List<String> names = new ArrayList<>();
        for (JsonElement element : docs.getAsJsonArray("eventValues")) {
            JsonObject value = element.getAsJsonObject();
            names.add(value.get("name").getAsString());
            if (value.get("name").getAsString().equals("event-item")) {
                item = value;
            }
        }
        assertFalse(names.contains("message"));
        assertEquals("event-damage", names.get(0));
        assertEquals("item", item.get("type").getAsString());
        assertEquals(List.of("event-held-item-change", "event-inventory-change", "event-start-using-item",
                        "event-stop-using-item", "event-consume", "event-item-break", "event-item-tooltip",
                        "event-durability-below"),
                strings(item.getAsJsonArray("events"), null));
    }
}
