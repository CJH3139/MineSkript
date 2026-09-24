package com.mineskript.client.world.elements;

import static com.mineskript.client.ClientEvents.state;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class WorldEvents {
    private WorldEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        state(registry, "Weather Change", "on weather change", "weather")
                .description("Fires when rain or thunder starts or stops in your current world, checked once per tick. It carries no values; use the it is raining and it is thundering conditions to see the new weather.")
                .examples("on weather change:",
                        "\tif it is thundering:",
                        "\t\tsend \"storm coming\"")
                .since("1.0.0-alpha.2");
        state(registry, "Dimension Change", "on (dimension change|world change)", "dimension")
                .values("from dimension", "to dimension")
                .description("Fires when you move to another dimension, checked once per tick. event-from dimension and event-to dimension are dimension ids such as minecraft:overworld, minecraft:the_nether and minecraft:the_end.")
                .examples("on dimension change:",
                        "\tif event-to dimension is \"minecraft:the_nether\":",
                        "\t\tsend \"welcome to the nether\"")
                .since("1.0.0-alpha.2");
        state(registry, "Block Break", "on (block break|break block|break of block)", "block break")
                .values("block", "block x", "block y", "block z", "location")
                .description("Fires when you break a block. event-block is the block type that was broken and event-block x, y and z are its coordinates; event-location is the same position as a location. Blocks broken by anyone else do not fire it.")
                .examples("on block break:",
                        "\tif event-block is stone:",
                        "\t\tadd 1 to {stone mined}",
                        "",
                        "on break block:",
                        "\tsend \"broke %event-block% at %event-block x% %event-block y% %event-block z%\"")
                .since("1.0.0-alpha.5");
        state(registry, "Block Place", "on (block place|place block|placing of block)", "block place")
                .values("block", "block x", "block y", "block z", "location")
                .description("Fires when a block you placed appears. MineSkript notes each right-click with a block item and watches the clicked position and the one next to it for up to 5 ticks; the first of them to change to a new, non-air block is reported. event-block is the placed block, event-block x, y and z its position and event-location that position as a location.",
                        "Because it watches for the change, it runs a tick or more after the click, and a block that changes there for another reason in that window can be reported.")
                .examples("on block place:",
                        "\tif event-block is obsidian:",
                        "\t\tsend \"obsidian placed\"",
                        "",
                        "on place block:",
                        "\tset {-last place y} to event-block y")
                .since("1.0.0-alpha.5");
        state(registry, "Time Change", "on time change", "time change")
                .values("time")
                .description("Fires when the time of day jumps instead of advancing normally: /time set, sleeping through the night and similar. It is checked once per tick and fires when the time differs from the expected next value by more than 20 ticks. event-time is the new time of day from 0 to 23999.",
                        "The first check after the event becomes active only records the time. Normal daylight progress, or time frozen by a game rule, never fires it.")
                .examples("on time change:",
                        "\tif event-time is less than 1000:",
                        "\t\tsend \"it is morning\"")
                .since("1.0.0-alpha.6");
    }
}
