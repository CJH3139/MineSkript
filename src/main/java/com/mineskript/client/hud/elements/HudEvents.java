package com.mineskript.client.hud.elements;

import static com.mineskript.client.ClientEvents.state;

import com.mineskript.lang.parse.SyntaxRegistry;

/** The screen events: screens opening and closing, toasts and advancements. */
public final class HudEvents {
    private HudEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        state(registry, "Screen Open", "on screen open", "screen open")
                .values("screen title", "screen type")
                .description("Fires when a screen opens while none was open: inventories, chests, the pause menu, the chat box and so on. It is polled once per tick, so switching straight from one screen to another does not fire it again. event-screen title is the screen title and event-screen type the name of the screen kind.",
                        "Opening chat with T counts as a screen too.")
                .examples("on screen open:",
                        "\tif event-screen title is \"Chest\":",
                        "\t\tsend \"opened a chest\"",
                        "",
                        "on screen open:",
                        "\tsend \"%event-screen type%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Screen Close", "on screen close", "screen close")
                .description("Fires when the last open screen closes and you are back in the game view, checked once per tick. It carries no values.")
                .examples("on screen close:",
                        "\tsend \"back in game\"")
                .since("1.0.0-alpha.2");
        state(registry, "Toast", "on toast", "toast")
                .values("text", "toast type")
                .description("Fires when a toast pops up in the top right corner. event-toast type is the kind of toast, such as AdvancementToast, RecipeToast or SystemToast. event-text is the advancement title for advancement toasts and empty for every other kind. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on toast:",
                        "\tsend \"toast: %event-toast type%\"")
                .since("1.0.0-alpha.6");
        state(registry, "Advancement", "on advancement [(get|earned|done|complete)]", "advancement")
                .values("text", "advancement")
                .description("Fires when an advancement toast is shown for you. event-advancement is the advancement id, such as minecraft:story/mine_stone, and event-text its title. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "It is triggered by the toast, so advancements that do not show a toast do not fire it. A toast event fires as well.")
                .examples("on advancement complete:",
                        "\tsend \"earned %event-text%\"")
                .since("1.0.0-alpha.6");
    }
}
