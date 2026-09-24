package com.mineskript.client.player.elements;

import static com.mineskript.client.ClientEvents.state;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class PlayerEvents {
    private PlayerEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        state(registry, "Move", "on move", "move")
                .values("from x", "from y", "from z", "to x", "to y", "to z")
                .description("Fires when the block you are standing in changes, that is when your whole-number x, y or z coordinate changes. It is polled once per tick, so small movements inside one block do not fire it, and a teleport fires it once. The old and new block coordinates are in event-from x/y/z and event-to x/y/z.")
                .examples("on move:",
                        "\tif event-to y is less than 0:",
                        "\t\tsend \"you are below y 0\"",
                        "",
                        "on move:",
                        "\tsend \"%event-from x% %event-from z% to %event-to x% %event-to z%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Jump", "on jump", "jump")
                .description("Fires on the tick you leave the ground while moving upward. It is detected by polling, so anything that launches you upward from the ground (a slime block, knockback) also counts as a jump.")
                .examples("on jump:",
                        "\tadd 1 to {jumps}",
                        "",
                        "on jump:",
                        "\tif player is sneaking:",
                        "\t\tsend \"sneak jump\"")
                .since("1.0.0-alpha.2");
        state(registry, "Land", "on land", "land")
                .values("fall distance")
                .description("Fires on the tick you touch the ground after being in the air. event-fall distance holds how far you fell, in blocks.")
                .examples("on land:",
                        "\tif event-fall distance is greater than 3:",
                        "\t\tsend \"fell %event-fall distance% blocks\"")
                .since("1.0.0-alpha.2");
        state(registry, "Sneak", "on sneak", "sneak")
                .description("Fires when you start sneaking, checked once per tick.")
                .examples("on sneak:",
                        "\tsend \"sneaking\"")
                .since("1.0.0-alpha.2");
        state(registry, "Stop Sneaking", "on (stop sneaking|unsneak)", "unsneak")
                .description("Fires when you stop sneaking, checked once per tick.")
                .examples("on unsneak:",
                        "\tsend \"standing up\"")
                .since("1.0.0-alpha.2");
        state(registry, "Sprint", "on sprint", "sprint")
                .description("Fires when you start sprinting, checked once per tick.")
                .examples("on sprint:",
                        "\tadd 1 to {-sprints}")
                .since("1.0.0-alpha.2");
        state(registry, "Stop Sprinting", "on (stop sprinting|unsprint)", "unsprint")
                .description("Fires when you stop sprinting for any reason (releasing the key, hunger, hitting a wall), checked once per tick.")
                .examples("on stop sprinting:",
                        "\tsend \"stopped sprinting\"")
                .since("1.0.0-alpha.2");
        state(registry, "Damage", "on (damage|damage taken|take damage)", "damage")
                .values("damage")
                .description("Fires when your health goes down but stays above zero. Health is polled once per tick, so several hits in the same tick arrive as one event, and changes smaller than 0.01 are ignored. event-damage is the health lost in health points (1 point is half a heart).",
                        "The hit that kills you fires on death instead, and damage soaked up by absorption hearts does not lower health, so it does not fire.")
                .examples("on damage:",
                        "\tsend \"took %event-damage% damage\"",
                        "",
                        "on take damage:",
                        "\tif health of player is less than 6:",
                        "\t\tshow title \"low health\"")
                .since("1.0.0-alpha.2");
        state(registry, "Heal", "on heal", "heal")
                .values("healed")
                .description("Fires when your health goes up while you are alive, polled once per tick. event-healed is the health gained in health points (1 point is half a heart). Coming back to life fires on respawn instead.")
                .examples("on heal:",
                        "\tsend \"healed %event-healed%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Health Change", "on health change", "health")
                .values("health change", "old health")
                .description("Fires whenever your health changes by at least 0.01, in either direction, including dying and respawning. It runs before the more specific damage, heal, death or respawn event of the same tick. event-health change is the signed difference and event-old health the value before.")
                .examples("on health change:",
                        "\tsend \"health %event-old health% to %health of player%\"",
                        "",
                        "on health change:",
                        "\tif event-old health is greater than 15:",
                        "\t\tsend \"hit while healthy\"")
                .since("1.0.0-alpha.5");
        state(registry, "Death", "on death", "death")
                .description("Fires when your health reaches zero, checked once per tick.")
                .examples("on death:",
                        "\tadd 1 to {deaths}",
                        "\tsend \"deaths so far: %{deaths}%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Respawn", "on respawn", "respawn")
                .description("Fires when your health goes from zero back above zero, which is when you respawn. It is detected from health, polled once per tick.")
                .examples("on respawn:",
                        "\tsend \"welcome back\"")
                .since("1.0.0-alpha.2");
        state(registry, "Hunger Change", "on hunger change", "hunger")
                .values("hunger change")
                .description("Fires when your food level changes, polled once per tick. event-hunger change is the signed change in food points (the bar holds 20, 2 per drumstick).")
                .examples("on hunger change:",
                        "\tif hunger of player is less than 7:",
                        "\t\tsend \"eat something\"")
                .since("1.0.0-alpha.2");
        state(registry, "Level Change", "on level change", "level")
                .values("level change")
                .description("Fires when your experience level changes up or down, polled once per tick. event-level change is the signed number of levels gained or lost.")
                .examples("on level change:",
                        "\tsend \"level changed by %event-level change%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Gamemode Change", "on gamemode change", "gamemode")
                .values("gamemode")
                .description("Fires when your game mode changes, checked once per tick. event-gamemode is the new mode: survival, creative, adventure or spectator.")
                .examples("on gamemode change:",
                        "\tif event-gamemode is \"creative\":",
                        "\t\tsend \"creative mode on\"")
                .since("1.0.0-alpha.2");
        state(registry, "XP Change", "on (xp change|experience change)", "xp")
                .values("xp change")
                .description("Fires when your total experience points change without your level changing in the same tick, polled once per tick. event-xp change is the signed number of experience points gained or lost.",
                        "A change that also changes your level fires level change instead of this event.")
                .examples("on xp change:",
                        "\tif event-xp change is greater than 0:",
                        "\t\tsend \"+%event-xp change% xp\"")
                .since("1.0.0-alpha.2");
        state(registry, "Effect Gain", "on effect gain", "effect gain")
                .values("effect", "effect level")
                .description("Fires for each status effect that becomes active on you, checked once per tick. event-effect is the effect name without the minecraft: prefix (for example speed) and event-effect level its level, where 1 means level I.",
                        "An effect that only changes level while it stays active does not fire this.")
                .examples("on effect gain:",
                        "\tsend \"got %event-effect% %event-effect level%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Effect Lose", "on (effect lose|effect loss)", "effect lose")
                .values("effect")
                .description("Fires for each status effect that is no longer active on you, whether it ran out or was removed (milk, death). It is checked once per tick, and event-effect is the effect name without the minecraft: prefix.")
                .examples("on effect lose:",
                        "\tif event-effect is \"night_vision\":",
                        "\t\tsend \"night vision ended\"")
                .since("1.0.0-alpha.2");
        state(registry, "Level Up", "on level up", "level up")
                .values("level change")
                .description("Fires when your experience level goes up, polled once per tick. event-level change is how many levels were gained. Level change fires as well on the same tick.")
                .examples("on level up:",
                        "\tif xp level is 30:",
                        "\t\tsend \"ready to enchant\"")
                .since("1.0.0-alpha.6");
    }
}
