package com.mineskript.client.player.elements;

import static com.mineskript.client.ClientEvents.filtered;
import static com.mineskript.client.ClientEvents.state;
import static com.mineskript.client.ClientEvents.states;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class PlayerEvents {
    private PlayerEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        state(registry, "Move", "on move", "move")
                .values("from x", "from y", "from z", "to x", "to y", "to z")
                .description("Fires when the block you are standing in changes, that is when your whole-number x, y or z coordinate changes. It is polled once per tick, so small movements inside one block do not fire it, and a teleport fires it once. The old and new block coordinates are in event-from x/y/z and event-to x/y/z.")
                .examples("on move:",
                        "	if event-to y is less than 0:",
                        "		send \"you are below y 0\"",
                        "",
                        "on move:",
                        "	send \"%event-from x% %event-from z% to %event-to x% %event-to z%\"")
                .since("1.0.0-alpha.2");
        states(registry, "Jump", "jump", "on [player] (jump|jumping)")
                .description("Fires on the tick you leave the ground while moving upward. It is detected by polling, so anything that launches you upward from the ground (a slime block, knockback) also counts as a jump. Also written on player jump, like Skript.")
                .examples("on jump:",
                        "	add 1 to {jumps}",
                        "",
                        "on jump:",
                        "	if player is sneaking:",
                        "		send \"sneak jump\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        state(registry, "Land", "on land", "land")
                .values("fall distance")
                .description("Fires on the tick you touch the ground after being in the air. fall distance (or event-fall distance) holds how far you fell, in blocks.")
                .examples("on land:",
                        "	if event-fall distance is greater than 3:",
                        "		send \"fell %event-fall distance% blocks\"",
                        "",
                        "on land:",
                        "	if fall distance is greater than 3:",
                        "		send \"fell %fall distance% blocks\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        state(registry, "Sneak", "on sneak", "sneak")
                .description("Fires when you start sneaking, checked once per tick.")
                .examples("on sneak:",
                        "	send \"sneaking\"")
                .since("1.0.0-alpha.2");
        state(registry, "Stop Sneaking", "on (stop sneaking|unsneak)", "unsneak")
                .description("Fires when you stop sneaking, checked once per tick.")
                .examples("on unsneak:",
                        "	send \"standing up\"")
                .since("1.0.0-alpha.2");
        states(registry, "Sneak Toggle", "sneak toggle", "on [player] (toggle|toggling) sneak",
                "on [player] sneak (toggle|toggling)")
                .description("Fires when you start or stop sneaking, like Skript's sneak toggle, checked once per tick. Use is sneaking to see which: it is already the new state. On sneak and on unsneak fire for one direction each.")
                .examples("on sneak toggle:",
                        "	if player is sneaking:",
                        "		send \"crouched\"",
                        "	else:",
                        "		send \"stood up\"",
                        "",
                        "on toggle sneak:",
                        "	add 1 to {-sneak toggles}")
                .since("1.0.0-alpha.11");
        state(registry, "Sprint", "on sprint", "sprint")
                .description("Fires when you start sprinting, checked once per tick.")
                .examples("on sprint:",
                        "	add 1 to {-sprints}")
                .since("1.0.0-alpha.2");
        state(registry, "Stop Sprinting", "on (stop sprinting|unsprint)", "unsprint")
                .description("Fires when you stop sprinting for any reason (releasing the key, hunger, hitting a wall), checked once per tick.")
                .examples("on stop sprinting:",
                        "	send \"stopped sprinting\"")
                .since("1.0.0-alpha.2");
        states(registry, "Sprint Toggle", "sprint toggle", "on [player] (toggle|toggling) sprint",
                "on [player] sprint (toggle|toggling)")
                .description("Fires when you start or stop sprinting, like Skript's sprint toggle, checked once per tick. Use is sprinting to see which: it is already the new state. On sprint and on stop sprinting fire for one direction each.")
                .examples("on sprint toggle:",
                        "	if player is sprinting:",
                        "		show action bar \"sprinting\"")
                .since("1.0.0-alpha.11");
        state(registry, "Damage", "on (damage|damage taken|take damage)", "damage")
                .values("damage", "old health")
                .description("Fires when your health goes down but stays above zero. Health is polled once per tick, so several hits in the same tick arrive as one event, and changes smaller than 0.01 are ignored. damage (or event-damage) is the health lost in health points (1 point is half a heart), and past health (or event-old health) your health before the hit.",
                        "The hit that kills you fires on death instead, and damage soaked up by absorption hearts does not lower health, so it does not fire.")
                .examples("on damage:",
                        "	send \"took %event-damage% damage\"",
                        "",
                        "on damage:",
                        "	send \"took %damage% damage, health was %past health%\"",
                        "",
                        "on take damage:",
                        "	if health of player is less than 6:",
                        "		show title \"low health\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "Heal", "heal", "on (heal|healing)")
                .values("healed", "old health")
                .description("Fires when your health goes up while you are alive, polled once per tick. event-healed is the health gained in health points (1 point is half a heart), and past health (or event-old health) your health before. Coming back to life fires on respawn instead.")
                .examples("on heal:",
                        "	send \"healed %event-healed%\"",
                        "",
                        "on healing:",
                        "	add event-healed to {-healed}")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        state(registry, "Health Change", "on health change", "health")
                .values("health change", "old health")
                .description("Fires whenever your health changes by at least 0.01, in either direction, including dying and respawning. It runs before the more specific damage, heal, death or respawn event of the same tick. event-health change is the signed difference and event-old health, also written past health, the value before.")
                .examples("on health change:",
                        "	send \"health %event-old health% to %health of player%\"",
                        "",
                        "on health change:",
                        "	if event-old health is greater than 15:",
                        "		send \"hit while healthy\"")
                .since("1.0.0-alpha.5");
        state(registry, "Death", "on death", "death")
                .description("Fires when your health reaches zero, checked once per tick.")
                .examples("on death:",
                        "	add 1 to {deaths}",
                        "	send \"deaths so far: %{deaths}%\"")
                .since("1.0.0-alpha.2");
        states(registry, "Respawn", "respawn", "on [player] (respawn|respawning)")
                .description("Fires when your health goes from zero back above zero, which is when you respawn. It is detected from health, polled once per tick. Also written on player respawn, like Skript.")
                .examples("on respawn:",
                        "	send \"welcome back\"",
                        "",
                        "on player respawn:",
                        "	add 1 to {-lives used}")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "Hunger Change", "hunger", "on hunger change",
                "on (food|hunger) (level|meter|metre|bar) (change|changing)")
                .values("hunger change")
                .description("Fires when your food level changes, polled once per tick. event-hunger change is the signed change in food points (the bar holds 20, 2 per drumstick). Also written on food level change or on hunger bar change, like Skript's hunger meter change.")
                .examples("on hunger change:",
                        "	if hunger of player is less than 7:",
                        "		send \"eat something\"",
                        "",
                        "on food bar change:",
                        "	send \"food %food level%\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "Level Change", "level", "on [player] level change")
                .values("level change")
                .description("Fires when your experience level changes up or down, polled once per tick. event-level change is the signed number of levels gained or lost. Also written on player level change, like Skript.")
                .examples("on level change:",
                        "	send \"level changed by %event-level change%\"",
                        "",
                        "on player level change:",
                        "	send \"now level %player's level%\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        filtered(registry, "Gamemode Change", "gamemode", "gamemode",
                "on (gamemode|game mode) change [to %-gamemode%]")
                .values("gamemode")
                .description("Fires when your game mode changes, checked once per tick. event-gamemode is the new mode: survival, creative, adventure or spectator.",
                        "Like Skript, on gamemode change to creative only fires for that new mode. The game mode must be written out, not taken from a variable.")
                .examples("on gamemode change:",
                        "	if event-gamemode is creative:",
                        "		send \"creative mode on\"",
                        "",
                        "on gamemode change to spectator:",
                        "	send \"now spectating\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "XP Change", "xp", "on (xp change|experience change)",
                "on [player] (level progress|xp|exp|experience) (change|update)")
                .values("xp change")
                .description("Fires when your total experience points change without your level changing in the same tick, polled once per tick. event-xp change is the signed number of experience points gained or lost.",
                        "A change that also changes your level fires level change instead of this event.")
                .examples("on xp change:",
                        "	if event-xp change is greater than 0:",
                        "		send \"+%event-xp change% xp\"",
                        "",
                        "on level progress change:",
                        "	send \"%level progress of player% of the way to the next level\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        filtered(registry, "Effect Gain", "effect gain", "effect", "on effect gain [of %-potioneffecttypes%]")
                .values("effect", "effect level")
                .description("Fires for each status effect that becomes active on you, checked once per tick. event-effect is the potion effect type (it prints as its name, such as night vision) and event-effect level its level, where 1 means level I.",
                        "on effect gain of speed only fires for that effect; the effects must be written out, not taken from a variable. An effect that only changes level while it stays active does not fire this.")
                .examples("on effect gain:",
                        "	send \"got %event-effect% %event-effect level%\"",
                        "",
                        "on effect gain of poison or wither:",
                        "	send \"drink milk\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        filtered(registry, "Effect Lose", "effect lose", "effect",
                "on (effect lose|effect loss) [of %-potioneffecttypes%]")
                .values("effect")
                .description("Fires for each status effect that is no longer active on you, whether it ran out or was removed (milk, death). It is checked once per tick, and event-effect is the potion effect type that ended.",
                        "on effect lose of night vision only fires for that effect; the effects must be written out.")
                .examples("on effect lose:",
                        "	if event-effect is night vision:",
                        "		send \"night vision ended\"",
                        "",
                        "on effect loss of speed:",
                        "	send \"slow again\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "Level Up", "level up", "on [player] level up")
                .values("level change")
                .description("Fires when your experience level goes up, polled once per tick. event-level change is how many levels were gained. Level change fires as well on the same tick.")
                .examples("on level up:",
                        "	if xp level is 30:",
                        "		send \"ready to enchant\"")
                .since("1.0.0-alpha.6", "1.0.0-alpha.11");
    }
}
