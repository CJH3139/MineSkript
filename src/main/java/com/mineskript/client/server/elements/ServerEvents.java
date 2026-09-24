package com.mineskript.client.server.elements;

import static com.mineskript.client.ClientEvents.state;
import static com.mineskript.client.ClientEvents.states;

import com.mineskript.lang.parse.SyntaxRegistry;

public final class ServerEvents {
    private ServerEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        states(registry, "World Join", "join", "on (world join|join server|server join)",
                "on (join|joining|login|logging in)")
                .description("Fires on the first tick you are in a world after having none: joining a server or opening a singleplayer world. Nothing else is compared on that tick, so events such as move or health change start from the following tick.",
                        "Skript's on join is a player joining the server; for a client that is you joining, so on join and on login are this event. On player join is someone else appearing in the tab list (see Player Join).")
                .examples("on join server:",
                        "\twait 2 seconds",
                        "\tsend \"joined, %players online% players here\"",
                        "",
                        "on world join:",
                        "\tset {-session deaths} to 0",
                        "",
                        "on join:",
                        "\tsend \"welcome back\"")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        states(registry, "World Leave", "leave", "on (world leave|leave server|server leave|quit server)",
                "on (quit|quitting|logout|log out|logging out|leave|leaving)")
                .description("Fires on the first tick after the world is gone, when you leave a server or close a singleplayer world. The world is already unloaded, so anything that reads the player or the world fails, and lines after a wait never run.",
                        "Skript's on quit is a player leaving the server; for a client that is you leaving, so on quit, on leave and on logout are this event. Also see on disconnect, which gives the reason when a server closes the connection.")
                .examples("on quit server:",
                        "\tset {last left} to \"left a world\"",
                        "",
                        "on quit:",
                        "\tadd 1 to {sessions}")
                .since("1.0.0-alpha.2", "1.0.0-alpha.11");
        state(registry, "Player Join", "on (player join|player join server|player joins server)", "player join")
                .values("player")
                .description("Fires when a name appears in the tab list of online players, checked once per tick. event-player is that name. The first list received after you join is taken as the starting point, so the players already online do not each fire it.",
                        "It follows the tab list, so servers that add fake entries to the tab list can fire it for names that are not real players.")
                .examples("on player join:",
                        "\tsend \"%event-player% joined\"")
                .since("1.0.0-alpha.2");
        state(registry, "Player Leave", "on (player leave|player leave server|player leaves server)", "player leave")
                .values("player")
                .description("Fires when a name disappears from the tab list of online players, checked once per tick. event-player is that name.")
                .examples("on player leaves server:",
                        "\tsend \"%event-player% left\"")
                .since("1.0.0-alpha.2");
        state(registry, "Tab List Change", "on tab list (change|update)", "tab list")
                .description("Fires when the server updates the tab list: its header or footer, or any player entry being added, updated or removed. It fires at most once per tick and carries no values. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on tab list change:",
                        "\tadd 1 to {-tab changes}")
                .since("1.0.0-alpha.6");
        state(registry, "Action Bar", "on (actionbar|action bar) [(update|change|message)]", "actionbar")
                .values("text")
                .description("Fires when the server sets the action bar text above your hotbar. event-text is the text with formatting removed. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Action bar lines sent as overlay chat messages, which some vanilla messages use, do not fire this and are not passed to on chat either.")
                .examples("on action bar:",
                        "\tif event-text contains \"Mana\":",
                        "\t\tset {-mana line} to event-text")
                .since("1.0.0-alpha.6");
        state(registry, "Title", "on title [(update|change)]", "title")
                .values("text")
                .description("Fires when the server sends a new title. event-text is the title text with formatting removed. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Titles shown by your own scripts with show title do not come from the server and do not fire this.")
                .examples("on title:",
                        "\tsend \"title: %event-text%\"")
                .since("1.0.0-alpha.6");
        state(registry, "Subtitle", "on subtitle [(update|change)]", "subtitle")
                .values("text")
                .description("Fires when the server sends a new subtitle. event-text is the subtitle text with formatting removed. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on subtitle:",
                        "\tsend \"subtitle: %event-text%\"")
                .since("1.0.0-alpha.6");
        state(registry, "Boss Bar", "on (bossbar|boss bar) [(update|change)]", "bossbar")
                .values("text", "bossbar change", "progress")
                .description("Fires when the server adds or removes a boss bar or changes its progress or name. event-text is the bar name and event-bossbar change says what happened: add, remove, progress or name. event-progress is the bar fill from 0 to 100. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "event-progress is only set for add and progress changes; reading it after a remove or name change is an error. Colour and style changes are not reported.")
                .examples("on boss bar update:",
                        "\tif event-bossbar change is \"progress\":",
                        "\t\tsend \"%event-text%: %event-progress%\"")
                .since("1.0.0-alpha.6");
        state(registry, "Scoreboard Update", "on scoreboard (update|change)", "scoreboard")
                .description("Fires when the server changes the scoreboard: a score set or reset, an objective added, changed or removed, or the displayed objective changed. It fires at most once per tick and carries no values. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.")
                .examples("on scoreboard change:",
                        "\tset {-scoreboard dirty} to true")
                .since("1.0.0-alpha.6");
        state(registry, "Sound", "on sound [(play|played)]", "sound")
                .values("sound", "x", "y", "z", "location")
                .description("Fires when your client plays a sound, whether it was sent by the server or made locally (footsteps, clicks). event-sound is the sound id, event-x, event-y and event-z its position and event-location that position as a location. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Sounds are very frequent; keep the trigger short and filter on event-sound first.")
                .examples("on sound:",
                        "\tif event-sound is \"minecraft:entity.creeper.primed\":",
                        "\t\tshow title \"creeper\"")
                .since("1.0.0-alpha.6");
        state(registry, "Particle", "on particle [(spawn|spawned)]", "particle")
                .values("particle", "count", "x", "y", "z", "location")
                .description("Fires when the server sends a particle effect. event-particle is the particle id, event-count the particle count in the packet, event-x, event-y and event-z the position and event-location that position as a location. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Particles the client makes by itself, such as block break or footstep particles, are not sent by the server and do not fire this.")
                .examples("on particle:",
                        "\tif event-particle is \"minecraft:totem_of_undying\":",
                        "\t\tsend \"someone popped a totem\"")
                .since("1.0.0-alpha.6");
        states(registry, "Chunk Load", "chunk load", "on chunk (load|loading)")
                .values("chunk x", "chunk z")
                .description("Fires when a chunk is loaded on your client. event-chunk x and event-chunk z are chunk coordinates (block coordinates divided by 16, rounded down). It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Joining a world loads hundreds of chunks at once, which can fill the per-tick signal limit and drop other signal events in those ticks.")
                .examples("on chunk load:",
                        "\tadd 1 to {-chunks loaded}")
                .since("1.0.0-alpha.6", "1.0.0-alpha.11");
        states(registry, "Chunk Unload", "chunk unload", "on chunk (unload|unloading)")
                .values("chunk x", "chunk z")
                .description("Fires when a chunk is unloaded from your client. event-chunk x and event-chunk z are its chunk coordinates. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "Chunks unloaded while leaving a world arrive when there is no world and are not delivered.")
                .examples("on chunk unload:",
                        "\tsend \"unloaded %event-chunk x%, %event-chunk z%\"")
                .since("1.0.0-alpha.6", "1.0.0-alpha.11");
        state(registry, "Client Tick", "on client tick", "client tick")
                .description("Fires once every client tick (20 times per second) while you are in a world, after timers, key presses and the other tick checks. It is the same as every tick but runs later in the tick.",
                        "Runs 20 times a second; keep the body short.")
                .examples("on client tick:",
                        "\tif player is in lava:",
                        "\t\tsend \"get out of the lava\"")
                .since("1.0.0-alpha.6");
        state(registry, "Render Frame", "on (render tick|frame|render frame)", "frame")
                .description("Fires once per rendered frame while you are in a world, so how often it runs depends on your frame rate. It runs at the start of the frame, before the game tick work of that frame.",
                        "This can run hundreds of times a second; heavy work here lowers your frame rate.")
                .examples("on frame:",
                        "\tadd 1 to {-frames}")
                .since("1.0.0-alpha.6");
        state(registry, "Scroll", "on [mouse] scroll", "scroll")
                .values("scroll")
                .description("Fires when the mouse wheel scrolls your hotbar. event-scroll is the vertical scroll amount reported by the mouse; its sign tells the direction. It is reported from a game hook, queued, and run at the end of the client tick, only while you are in a world. At most 256 queued signals of all kinds are kept per tick, so extra ones in a very busy tick are dropped.",
                        "It is based on hotbar scrolling, so scrolling inside screens and menus does not fire it.")
                .examples("on mouse scroll:",
                        "\tsend \"scrolled %event-scroll%\"")
                .since("1.0.0-alpha.6");
        state(registry, "Disconnect", "on [server] disconnect", "disconnect")
                .values("reason")
                .description("Fires when the connection to the server is closed. event-reason is the disconnect reason text with formatting removed. It is queued like other signal events and runs on the next client tick, and unlike them it runs even though no world is loaded by then.",
                        "There is no world at this point, so only work with variables and text; lines that read the player fail and waits are dropped.")
                .examples("on server disconnect:",
                        "\tset {last disconnect} to event-reason")
                .since("1.0.0-alpha.6");
    }
}
