package com.mineskript.client.entity.elements;

import static com.mineskript.client.ClientEvents.state;

import com.mineskript.lang.parse.SyntaxRegistry;

/** The entity events: mounting, and entities spawning, despawning and dying. */
public final class EntityEvents {
    private EntityEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        state(registry, "Mount", "on mount", "mount")
                .values("entity")
                .description("Fires when you start riding something, such as a horse, boat or minecart, checked once per tick. event-entity is the vehicle.",
                        "Moving straight from one vehicle to another fires dismount for the old one and then mount for the new one.")
                .examples("on mount:",
                        "\tsend \"riding %event-entity%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Dismount", "on dismount", "dismount")
                .values("entity")
                .description("Fires when you stop riding a vehicle, checked once per tick. event-entity is the vehicle you left.")
                .examples("on dismount:",
                        "\tif event-entity is \"horse\":",
                        "\t\tsend \"left the horse\"")
                .since("1.0.0-alpha.2");
        state(registry, "Entity Spawn", "on entity spawn", "entity spawn")
                .values("entity")
                .description("Fires when an entity is loaded on your client, other than yourself. That includes newly spawned mobs, but also dropped items, arrows, and any entity or player that comes into range. event-entity is the entity. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on entity spawn:",
                        "\tif event-entity is \"creeper\":",
                        "\t\tsend \"creeper %distance of event-entity% blocks away\"")
                .since("1.0.0-alpha.6");
        state(registry, "Entity Despawn", "on entity despawn", "entity despawn")
                .values("entity")
                .description("Fires when an entity other than you is unloaded from your client without having died: it moved out of range, was picked up, despawned or was removed. event-entity is the entity. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on entity despawn:",
                        "\tif event-entity is \"item\":",
                        "\t\tadd 1 to {-items gone}")
                .since("1.0.0-alpha.6");
        state(registry, "Entity Death", "on entity death", "entity death")
                .values("entity")
                .description("Fires when a living entity other than you is unloaded from your client while dead. This happens when its body disappears after the death animation, so it arrives shortly after the actual death. event-entity is the entity. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on entity death:",
                        "\tsend \"%name of event-entity% died\"")
                .since("1.0.0-alpha.6");
    }
}
