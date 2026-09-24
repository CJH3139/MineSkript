package com.mineskript.client.world.elements;

import static com.mineskript.client.ClientEvents.filtered;
import static com.mineskript.client.ClientEvents.state;
import static com.mineskript.client.ClientEvents.states;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class WorldEvents {
    private WorldEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        filtered(registry, "Weather Change", "weather", "weather", "on weather change [to %-weathertypes%]")
                .values("weather")
                .description("Fires when rain or thunder starts or stops in your current world, checked once per tick. event-weather is the new weather: clear, rain or thunder (thunder always comes with rain). The it is raining and it is thundering conditions work too.",
                        "Like Skript, on weather change to rain only fires for that new weather. The weather is written as clear (also sun or sunny), rain (also rainy or raining) or thunder (also thundering or thunderstorm), not taken from a variable.")
                .examples("on weather change:",
                        "	if it is thundering:",
                        "		send \"storm coming\"",
                        "",
                        "on weather change to thunder:",
                        "	show title \"thunderstorm\"",
                        "",
                        "on weather change:",
                        "	if event-weather is clear:",
                        "		send \"the sky cleared\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "Dimension Change", "dimension", "on (dimension change|world change)",
                "on [player] world (change|changing|changed)")
                .values("from dimension", "to dimension")
                .description("Fires when you move to another dimension, checked once per tick. event-from dimension and event-to dimension are dimension ids such as minecraft:overworld, minecraft:the_nether and minecraft:the_end.")
                .examples("on dimension change:",
                        "	if event-to dimension is \"minecraft:the_nether\":",
                        "		send \"welcome to the nether\"",
                        "",
                        "on player world change:",
                        "	send \"now in %world of player%\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        filtered(registry, "Block Break", "block break", "block", "on (block break|break block|break of block)",
                "on [block] (break|breaking|mine|mining) [[of] %-itemtypes%]")
                .values("block", "block x", "block y", "block z", "location")
                .description("Fires when you break a block. event-block is the block type that was broken and event-block x, y and z are its coordinates; event-location is the same position as a location. Blocks broken by anyone else do not fire it.",
                        "Like Skript it is also written on break or on mine, and on break of stone (or on mine of diamond ore or deepslate diamond ore) only fires for those blocks. The blocks must be written out, not taken from a variable. Unlike Skript's on mine, MineSkript cannot tell whether the block dropped anything, so on mine fires for every block you break.")
                .examples("on block break:",
                        "	if event-block is stone:",
                        "		add 1 to {stone mined}",
                        "",
                        "on break block:",
                        "	send \"broke %event-block% at %event-block x% %event-block y% %event-block z%\"",
                        "",
                        "on mine of diamond ore or deepslate diamond ore:",
                        "	add 1 to {diamonds found}")
                .since("1.0.0-alpha.5", "1.0.0-alpha.11");
        filtered(registry, "Block Place", "block place", "block", "on (block place|place block|placing of block)",
                "on [block] (place|placing|build|building) [[of] %-itemtypes%]")
                .values("block", "block x", "block y", "block z", "location")
                .description("Fires when a block you placed appears. MineSkript notes each right-click with a block item and watches the clicked position and the one next to it for up to 5 ticks; the first of them to change to a new, non-air block is reported. event-block is the placed block, event-block x, y and z its position and event-location that position as a location.",
                        "Because it watches for the change, it runs a tick or more after the click, and a block that changes there for another reason in that window can be reported.",
                        "Like Skript it is also written on place, and on place of torch only fires for those blocks. The blocks must be written out, not taken from a variable.")
                .examples("on block place:",
                        "	if event-block is obsidian:",
                        "		send \"obsidian placed\"",
                        "",
                        "on place block:",
                        "	set {-last place y} to event-block y",
                        "",
                        "on place of torch:",
                        "	add 1 to {torches placed}")
                .since("1.0.0-alpha.5", "1.0.0-alpha.11");
        state(registry, "Time Change", "on time change", "time change")
                .values("time")
                .description("Fires when the time of day jumps instead of advancing normally: /time set, sleeping through the night and similar. It is checked once per tick and fires when the time differs from the expected next value by more than 20 ticks. event-time is the new time of day from 0 to 23999.",
                        "The first check after the event becomes active only records the time. Normal daylight progress, or time frozen by a game rule, never fires it.")
                .examples("on time change:",
                        "	if event-time is less than 1000:",
                        "		send \"it is morning\"")
                .since("1.0.0-alpha.6");
    }
}
