package com.mineskript.client.entity.elements;

import static com.mineskript.client.ClientEvents.filtered;
import static com.mineskript.client.ClientEvents.states;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class EntityEvents {
    private EntityEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        states(registry, "Mount", "mount", "on (mount|mounting)")
                .values("entity")
                .description("Fires when you start riding something, such as a horse, boat or minecart, checked once per tick. event-entity is the vehicle.",
                        "Moving straight from one vehicle to another fires dismount for the old one and then mount for the new one.")
                .examples("on mount:",
                        "\tsend \"riding %event-entity%\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "Dismount", "dismount", "on (dismount|dismounting)")
                .values("entity")
                .description("Fires when you stop riding a vehicle, checked once per tick. event-entity is the vehicle you left.")
                .examples("on dismount:",
                        "\tif event-entity is \"horse\":",
                        "\t\tsend \"left the horse\"",
                        "",
                        "on dismounting:",
                        "\tif event-entity is boat:",
                        "\t\tsend \"back on land\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        filtered(registry, "Entity Spawn", "entity spawn", "entity", "on entity spawn [of %-entitytypes%]",
                "on (spawn|spawning) [of %-entitytypes%]")
                .values("entity")
                .description("Fires when an entity is loaded on your client, other than yourself. That includes newly spawned mobs, but also dropped items, arrows, and any entity or player that comes into range. event-entity is the entity. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Like Skript it is also written on spawn, and on spawn of zombie (or on entity spawn of zombie or husk) only fires for those entity types. The types must be written out, not taken from a variable.")
                .examples("on entity spawn:",
                        "\tif event-entity is \"creeper\":",
                        "\t\tsend \"creeper %distance of event-entity% blocks away\"",
                        "",
                        "on spawn of a creeper:",
                        "\tshow title \"creeper nearby\"")
                .since("1.0.0-alpha.6", "1.0.0-alpha.11");
        filtered(registry, "Entity Despawn", "entity despawn", "entity", "on entity despawn [of %-entitytypes%]")
                .values("entity")
                .description("Fires when an entity other than you is unloaded from your client without having died: it moved out of range, was picked up, despawned or was removed. event-entity is the entity. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on entity despawn:",
                        "\tif event-entity is \"item\":",
                        "\t\tadd 1 to {-items gone}",
                        "",
                        "on entity despawn of item:",
                        "\tadd 1 to {-items gone}")
                .since("1.0.0-alpha.6", "1.0.0-alpha.11");
        filtered(registry, "Entity Death", "entity death", "entity", "on entity death [of %-entitytypes%]",
                "on death of %entitytypes%")
                .values("entity")
                .description("Fires when a living entity other than you is unloaded from your client while dead. This happens when its body disappears after the death animation, so it arrives shortly after the actual death. event-entity is the entity. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Like Skript's death event it can be written on death of zombie (or on death of a wither or ender dragon), which only fires for those entity types; the types must be written out. Plain on death is your own death (see Death), so on death of player means another player's body disappearing.")
                .examples("on entity death:",
                        "\tsend \"%name of event-entity% died\"",
                        "",
                        "on death of zombie:",
                        "\tadd 1 to {zombies killed}",
                        "",
                        "on death of a wither or ender dragon:",
                        "\tshow title \"boss down\"")
                .since("1.0.0-alpha.6", "1.0.0-alpha.11");
    }
}
