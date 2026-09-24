package com.mineskript.common.elements.events;

import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.List;
import java.util.Map;

/**
 * Defines every value a built-in event can provide, each once with one description. The events of every module
 * declare which of these they provide by name, and the generic event-name expression reads them, so the common
 * module defines them all before any other syntax is registered.
 */
public final class EventValues {
    private static final Map<String, String> AXIS_DESCRIPTIONS = Map.ofEntries(
            Map.entry("block x",
                    "The x coordinate of the broken or placed block."),
            Map.entry("x",
                    "The x coordinate where the sound played or the particles appeared (not rounded to a block)."),
            Map.entry("from x",
                    "Your block x coordinate before the move."),
            Map.entry("to x",
                    "Your block x coordinate after the move."),
            Map.entry("block y",
                    "The y coordinate of the broken or placed block."),
            Map.entry("y",
                    "The y coordinate where the sound played or the particles appeared (not rounded to a block)."),
            Map.entry("from y",
                    "Your block y coordinate before the move."),
            Map.entry("to y",
                    "Your block y coordinate after the move."),
            Map.entry("block z",
                    "The z coordinate of the broken or placed block."),
            Map.entry("z",
                    "The z coordinate where the sound played or the particles appeared (not rounded to a block)."),
            Map.entry("from z",
                    "Your block z coordinate before the move."),
            Map.entry("to z",
                    "Your block z coordinate after the move."));

    private EventValues() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEventValue(new EventValue("message", SkType.TEXT, "message",
                "The chat message, or the command without its leading slash."));
        add(registry, "damage", SkType.NUMBER,
                "Health lost in this hit, in health points (1 point is half a heart, 20 is a full bar without bonuses). Always positive.");
        add(registry, "healed", SkType.NUMBER,
                "Health gained, in health points (1 point is half a heart). Always positive.");
        add(registry, "health change", SkType.NUMBER,
                "Signed change in health, in health points: new health minus old health. Negative when hurt, positive when healed.");
        add(registry, "old health", SkType.NUMBER,
                "Your health just before the change, in health points (0 to your max health, 20 by default).");
        add(registry, "durability", SkType.NUMBER,
                "Uses left on the held item after the drop: its max durability minus its damage.");
        add(registry, "block", SkType.BLOCKTYPE,
                "The block type that was broken or placed, for example stone. Compare it with a block name such as event-block is stone.");
        add(registry, "screen title", SkType.TEXT,
                "The title text of the screen that opened, such as Chest or Crafting. Can be empty for screens without a title.");
        add(registry, "screen type", SkType.TEXT,
                "The name of the kind of screen that opened, as its class name, such as InventoryScreen or ChatScreen.");
        add(registry, "hunger change", SkType.NUMBER,
                "Signed change in food level, in food points (the bar holds 20, 2 per drumstick). Negative when hunger drops.");
        add(registry, "level change", SkType.NUMBER,
                "Signed number of experience levels gained or lost. In on level up it is always positive.");
        add(registry, "text", SkType.TEXT,
                "The text of the action bar, title, subtitle, boss bar name, or advancement title, with colours and formatting removed. In on toast it is empty unless the toast is an advancement toast.");
        add(registry, "bossbar change", SkType.TEXT,
                "What happened to the boss bar: add, remove, progress or name.");
        add(registry, "progress", SkType.NUMBER,
                "The boss bar fill as a percentage from 0 to 100. Only set when the change is add or progress.");
        add(registry, "sound", SkType.TEXT,
                "The id of the sound that played, such as minecraft:entity.player.levelup.");
        add(registry, "particle", SkType.TEXT,
                "The id of the particle type, such as minecraft:flame.");
        add(registry, "count", SkType.NUMBER,
                "The particle count sent by the server for this effect.");
        add(registry, "chunk x", SkType.NUMBER,
                "The chunk x coordinate: block x divided by 16, rounded down.");
        add(registry, "chunk z", SkType.NUMBER,
                "The chunk z coordinate: block z divided by 16, rounded down.");
        add(registry, "scroll", SkType.NUMBER,
                "The vertical scroll amount reported by the mouse wheel; the sign gives the direction.");
        add(registry, "toast type", SkType.TEXT,
                "The kind of toast, as its class name, such as AdvancementToast, RecipeToast or SystemToast.");
        add(registry, "advancement", SkType.TEXT,
                "The id of the advancement, such as minecraft:story/mine_stone.");
        add(registry, "reason", SkType.TEXT,
                "The reason the connection closed, as plain text with formatting removed.");
        add(registry, "time", SkType.NUMBER,
                "The new time of day in ticks, from 0 to 23999 (0 is sunrise, 6000 noon, 18000 midnight).");
        add(registry, "xp change", SkType.NUMBER,
                "Signed change in total experience points. Negative when points are spent or lost.");
        add(registry, "fall distance", SkType.NUMBER,
                "How far you fell before landing, in blocks.");
        add(registry, "item", SkType.ITEM,
                "The item involved in the event. For held item change it is the item in the newly selected slot, for inventory change the new content of the changed slot (air if emptied), for start and stop using item the main hand item, and for consume, item break and durability below the item that was eaten, broke or wore down.");
        add(registry, "previous item", SkType.ITEM,
                "The item in the hotbar slot you switched away from.");
        add(registry, "gamemode", SkType.TEXT,
                "The new game mode: survival, creative, adventure or spectator.");
        add(registry, "effect", SkType.TEXT,
                "The effect name without the minecraft: prefix, for example speed or night_vision.");
        add(registry, "effect level", SkType.NUMBER,
                "The level of the new effect, where 1 means level I.");
        add(registry, "entity", SkType.ENTITY,
                "The entity involved: the vehicle for mount and dismount, or the entity that spawned, despawned or died. Compare it with a type name, as in event-entity is \"zombie\".");
        add(registry, "from dimension", SkType.TEXT,
                "The id of the dimension you left, such as minecraft:overworld.");
        add(registry, "to dimension", SkType.TEXT,
                "The id of the dimension you arrived in, such as minecraft:the_nether.");
        add(registry, "player", SkType.TEXT,
                "The name of the player who joined or left, as shown in the tab list.");
        for (String axis : List.of("x", "y", "z")) {
            for (String name : List.of("block " + axis, axis, "from " + axis, "to " + axis)) {
                add(registry, name, SkType.NUMBER, AXIS_DESCRIPTIONS.get(name));
            }
        }
    }

    private static void add(SyntaxRegistry registry, String name, SkType type, String description) {
        registry.addEventValue(EventValue.of(name, type, description));
    }
}
