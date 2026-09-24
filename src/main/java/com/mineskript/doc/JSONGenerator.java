package com.mineskript.doc;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mineskript.api.AddonLoader;
import com.mineskript.api.MineSkriptAddon;
import com.mineskript.lang.ast.EventContext;
import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.function.FunctionInfo;
import com.mineskript.lang.function.FunctionParameter;
import com.mineskript.lang.parse.EventInfo;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class JSONGenerator {
    private JSONGenerator() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("usage: JSONGenerator <output.json> [version]");
            System.exit(2);
        }
        JsonObject root = generate(args.length > 1 ? args[1] : "unknown");
        Path out = Path.of(args[0]);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        Files.writeString(out, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root), StandardCharsets.UTF_8);
        System.out.println("wrote " + count(root) + " documented elements to " + out.toAbsolutePath());
    }

    public static JsonObject generate(String version) {
        return generate(version, List.of());
    }

    public static JsonObject generate(String version, List<? extends MineSkriptAddon> addons) {
        SyntaxRegistry registry = AddonLoader.load(() -> {
            SyntaxRegistry documenting = SyntaxRegistry.documenting();
            DefaultSyntax.registerAll(documenting);
            return documenting;
        }, addons).registry();

        JsonArray events = new JsonArray();
        JsonArray conditions = new JsonArray();
        JsonArray effects = new JsonArray();
        JsonArray expressions = new JsonArray();
        JsonArray functions = new JsonArray();
        Map<String, List<String>> eventIdsByValue = new HashMap<>();
        List<String> ids = new ArrayList<>();

        for (SyntaxRegistry.Registration registration : registry.registrations()) {
            switch (registration.kind()) {
                case "event" -> events.add(event(registration, ids, eventIdsByValue));
                case "condition" -> add(conditions, registration, ids);
                case "effect" -> add(effects, registration, ids);
                case "expression" -> add(expressions, registration, ids);
                case "function" -> functions.add(function(registration, ids));
                default -> {
                }
            }
        }

        JsonArray eventValues = new JsonArray();
        for (EventValue value : registry.eventValues()) {
            if (!value.generic()) {
                continue;
            }
            JsonObject object = eventValue(value);
            object.add("events", array(eventIdsByValue.getOrDefault(value.name(), List.of())));
            eventValues.add(object);
        }

        JsonObject root = new JsonObject();
        root.addProperty("version", version);
        root.add("events", events);
        root.add("conditions", conditions);
        root.add("effects", effects);
        root.add("expressions", expressions);
        root.add("eventValues", eventValues);
        root.add("functions", functions);
        return root;
    }

    private static JsonObject event(SyntaxRegistry.Registration registration, List<String> ids, Map<String, List<String>> eventIdsByValue) {
        EventInfo info = registration.event();
        JsonObject element = new JsonObject();
        String id = unique("event-" + slug(info.name()), ids);
        element.addProperty("id", id);
        element.addProperty("name", info.name());
        element.add("description", array(info.description()));
        element.add("examples", array(info.examples()));
        element.add("since", array(info.since()));
        element.add("keywords", array(info.keywords()));
        element.add("patterns", array(info.patterns()));
        EventContext context = info.context();
        element.addProperty("cancellable", context.cancellable());
        element.addProperty("instant", context.instant());
        JsonArray values = new JsonArray();
        for (EventValue value : context.values()) {
            values.add(eventValue(value));
            eventIdsByValue.computeIfAbsent(value.name(), key -> new ArrayList<>()).add(id);
        }
        element.add("eventValues", values);
        element.addProperty("addon", registration.addon());
        element.addProperty("module", module(registration));
        return element;
    }

    private static JsonObject function(SyntaxRegistry.Registration registration, List<String> ids) {
        FunctionInfo function = registration.function();
        JsonObject element = new JsonObject();
        element.addProperty("id", unique("function-" + slug(function.name()), ids));
        element.addProperty("name", function.name());
        element.addProperty("signature", function.signature());
        JsonArray parameters = new JsonArray();
        for (FunctionParameter parameter : function.parameters()) {
            JsonObject object = new JsonObject();
            object.addProperty("name", parameter.name());
            object.addProperty("type", typeName(parameter.type()));
            object.addProperty("list", parameter.list());
            parameter.defaultText().ifPresent(text -> object.addProperty("default", text));
            parameters.add(object);
        }
        element.add("parameters", parameters);
        element.addProperty("returnType", typeName(function.returnType()));
        element.addProperty("returnsList", function.listResult());
        element.add("description", array(function.description()));
        element.add("examples", array(function.examples()));
        element.add("since", array(function.since()));
        element.add("keywords", array(function.keywords()));
        element.addProperty("addon", registration.addon());
        element.addProperty("module", module(registration));
        return element;
    }

    private static void add(JsonArray into, SyntaxRegistry.Registration registration, List<String> ids) {
        Class<?> owner = registration.owner();
        if (owner == null || owner.isAnnotationPresent(NoDoc.class)) {
            return;
        }
        JsonObject existing = null;
        for (int i = 0; i < into.size(); i++) {
            JsonObject candidate = into.get(i).getAsJsonObject();
            if (candidate.get("class").getAsString().equals(owner.getName())) {
                existing = candidate;
            }
        }
        if (existing != null) {
            JsonArray patterns = existing.getAsJsonArray("patterns");
            registration.patterns().forEach(patterns::add);
            return;
        }
        JsonObject element = new JsonObject();
        element.addProperty("id", unique(owner.getSimpleName(), ids));
        element.addProperty("class", owner.getName());
        element.addProperty("name", owner.isAnnotationPresent(Name.class) ? owner.getAnnotation(Name.class).value() : owner.getSimpleName());
        element.add("description", array(owner.isAnnotationPresent(Description.class) ? List.of(owner.getAnnotation(Description.class).value()) : List.of()));
        element.add("examples", array(examples(owner)));
        element.add("since", array(owner.isAnnotationPresent(Since.class) ? List.of(owner.getAnnotation(Since.class).value()) : List.of()));
        element.add("keywords", array(owner.isAnnotationPresent(Keywords.class) ? List.of(owner.getAnnotation(Keywords.class).value()) : List.of()));
        element.add("events", array(owner.isAnnotationPresent(Events.class) ? List.of(owner.getAnnotation(Events.class).value()) : List.of()));
        element.add("patterns", array(registration.patterns()));
        if (registration.returnType() != null) {
            element.addProperty("returnType", typeName(registration.returnType()));
        }
        element.addProperty("addon", registration.addon());
        element.addProperty("module", module(registration));
        into.add(element);
    }

    private static String module(SyntaxRegistry.Registration registration) {
        return registration.module() == null ? "" : registration.module();
    }

    private static List<String> examples(Class<?> owner) {
        List<String> lines = new ArrayList<>();
        if (owner.isAnnotationPresent(Examples.class)) {
            lines.addAll(List.of(owner.getAnnotation(Examples.class).value()));
        }
        for (Example example : owner.getAnnotationsByType(Example.class)) {
            if (!lines.isEmpty()) {
                lines.add("");
            }
            lines.addAll(example.value().strip().lines().toList());
        }
        return lines;
    }

    private static JsonObject eventValue(EventValue value) {
        JsonObject object = new JsonObject();
        object.addProperty("name", value.syntax());
        object.addProperty("type", typeName(value.type()));
        object.addProperty("description", value.description() == null ? "" : value.description());
        return object;
    }

    private static JsonArray array(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private static String unique(String base, List<String> ids) {
        String id = base;
        for (int n = 2; ids.contains(id); n++) {
            id = base + "-" + n;
        }
        ids.add(id);
        return id;
    }

    private static String slug(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }

    private static String typeName(SkType type) {
        return Converters.typeName(type);
    }

    private static int count(JsonObject root) {
        return root.getAsJsonArray("events").size() + root.getAsJsonArray("conditions").size()
                + root.getAsJsonArray("effects").size() + root.getAsJsonArray("expressions").size()
                + root.getAsJsonArray("functions").size();
    }
}
