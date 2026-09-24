# MineSkript
**MineSkript** is a client-side Fabric mod where you write what you want your client to do in plain English. Scripts
are text files you drop in a folder, they reload without restarting the game, and they react to ticks, key presses,
chat and sixty other things that happen around you.

The language is modelled on [Skript](https://github.com/SkriptLang/Skript), so `wait 5 seconds`,
`if health of player is less than 6` and `send "hello"` all mean what a Skript user would expect. The interpreter is
written from scratch in Java rather than embedding Python or Lua, which is what lets a script pause mid-line without
blocking the game.

Everything runs on your own client. There is no server component, nothing is installed on servers you join, and the
mod only ever does what you could do by hand.

> [!IMPORTANT]
> MineSkript is in **early release**. Syntax may still change between versions.

## Why a custom language?
Modding a client normally means Java, mappings, a build system and an API surface to learn before you can make the
game do anything at all. MineSkript replaces that with near-English syntax in a text file: `if block below player is
stone:` does what it reads like, and you press reload instead of recompiling.

It can also stop a script mid-line and pick it up later, which is the thing
[Minescript](https://github.com/maxuser0/minescript) cannot. Python has no way to freeze a half-finished statement, so
scripts there live on their own threads and every game call hops back to the render thread a tick late, against a
world that may already have moved. Because the interpreter is ours, a trigger runs on the render thread itself:
`wait 20 ticks` hands control straight back to the game, and twenty ticks later the next line continues from exactly
where it stopped, reading the world as it really is.

The cost is that the language only does what has been built into it. That trade is the point.

## Requirements
- Minecraft **26.2**
- **Fabric Loader** 0.19.5 or newer
- **Fabric API** 0.160.0+26.2
- A **Java 25** runtime

## Support
Questions, bug reports and scripts to share: join the [support Discord](https://discord.gg/zSTDbB74wU).

## Download
Grab the latest jar from [Releases](../../releases), or build it yourself with the instructions under
[Building](#building).

## Getting started
Drop the jar and Fabric API in your `mods` folder and launch once. MineSkript creates `.minecraft/mineskript/` and
writes an `example.ms` into it. Scripts are plain text files ending in `.ms`; edit one and run `/ms reload` to see the
change without leaving the world.

```
on key press of "r":
    if block below player is stone or cobblestone:
        send "standing on stone at y %player's y-coordinate%"
        hold attack
        wait 20 ticks
        release attack

every 5 seconds:
    if health of player is less than 6:
        show title "low health"
```

## What you can write
Events for movement, health, hunger, xp, the inventory, held items, effects, mounts, weather, screens, dimensions,
players joining and leaving, your own outgoing chat, and more. Conditions on what you are holding, standing in,
looking at and carrying. Effects that press keys, aim the camera, pick hotbar slots, show titles, play sounds and run
commands. Variables in three scopes, arithmetic, `and`, `or` and `not`, chained comparisons like `1 < {x} < 5`,
`true` and `false`, loops, text and maths functions, `"42" parsed as number`, `wait until <condition>`, and
`try:` with `on error:` to catch a failing line (the message is in `{_error}`). Blocks broken and placed, health
changes, tools running low with `on durability below 10`, and the title of any screen that opens are all events.
`stop all scripts` and `stop script "name"` cancel what is running.

Scripts can also react to the action bar, titles and boss bars, the tab list and scoreboard, sounds and particles the
server sends, entities spawning, despawning and dying, chunks loading, every client tick or rendered frame, key combos
like `on key press of "ctrl+shift+x"`, the scroll wheel, toasts and advancements, level ups, time jumps, and being
disconnected (`event-reason` says why). `cancel event` inside `on chat send` stops the message from ever leaving.

A line ending in `\` carries on onto the next one, and everything between two `###` lines is a comment.

Functions take typed parameters, which arrive as local variables, and can return a value. Called on its own line, a
function may `wait`; used as a value, it must not. `local function` keeps it private to its file, and a function that
calls itself more than 100 levels deep stops with an error instead of freezing the game. An `options:` block defines
constants you paste in with `{@name}`.

```
options:
    greeting: hello

function label(n: number, unit: text = "blocks") :: text:
    return "%{_n}% %{_unit}%" if {_n} is not 1 else "1 block"

on key press of "g":
    if {home} is not set:
        send "{@greeting}, no home yet"
    loop "a,b,c" split at ",":
        send label(loop-iteration)
```

List variables work as in Skript. `{homes::alex}` is one entry, `{homes::*}` is the whole list, and a name part
written as `%expression%` is worked out when the line runs, so `{homes::%player%}` is a different entry per player.
`set {l::*} to a, b and c`, `add x to {l::*}`, `remove x from {l::*}`, `remove all x from {l::*}` and
`delete {l::*}` change a list; `loop {l::*}` sets `loop-value` and `loop-index`, and `size of {l::*}` counts it.
Entries keep the order they were first set in rather than being sorted, variable names are case-insensitive, and saved
lists survive a restart like any other `{global}` variable. Inside text, `""` is a literal quote:
`send "she said ""hi"""`.

Set, add and remove also work on some values of the game: `add 90 to yaw`, `remove 10 from pitch`,
`add 1 to selected slot` (which wraps round the hotbar), `set clipboard to "..."`, and `set message to "..."` inside
`on chat send` or `on command send`, which changes what is actually sent.

Items carry their data, the modern replacement for NBT: `custom name of held item` (none if it was never renamed),
`lore of held item`, `enchantments of held item` (a list like `sharpness 5`), `level of enchantment "sharpness" on
held item`, `custom model data of held item`, and any data component as text with
`component "minecraft:custom_data" of held item`.

Some things only you see. `on item tooltip` runs while the game draws the tooltip of the item you hover over, and
`add "&7worth: 5 gold" to the tooltip` (or `to the top of the tooltip`) adds your own lines, with `&` colour codes.
These triggers run instantly, so they cannot `wait`. `spawn a hologram "&6shop" at 0.5, 66, 0.5`,
`spawn an item display of diamond at ...` and `spawn a block display of gold block at ...` create client-side
entities; keep `last spawned client entity` in a variable to `move client entity {h} to x, y, z`,
`set text of hologram {h} to "..."` or `remove client entity {h}`. `show a "red" beam at 100, 64, -200` draws a
beacon beam without a beacon, and `remove all beams` clears them. None of this is sent to the server, and it all
disappears when you leave the world or reload the script.

The example scripts in your `mineskript` folder are the working reference, and `/ms help` lists the commands. A parse
error names the file, the line and the text it objected to.

## Effect commands
Type one line of MineSkript straight into the chat box, prefixed with `?`, and it runs instead of being sent:

```
?make me say "Hello!"
?set {-block} to cobblestone
?show title "test"
```

`me` and `myself` work anywhere `player` does. The prefix is configurable in `mineskript/config.txt`, which is created
once with its defaults and never overwritten, so your edits survive. A line beginning with the prefix never reaches
the server unless you switch the feature off.

## Commands
```
/ms reload                 every script
/ms reload <file.ms>       just that one, the others keep running
/ms reload config          re-read config.txt
/ms reload variables       re-read saved variables
/ms reload all             the config, variables and every script
/ms list                   loaded scripts, with trigger and error counts
/ms errors                 the errors from the last load, with the offending lines
/ms info                   version, folder and counts
/ms help                   the tree
```

`/ms` is short for `/mineskript`. File names tab-complete, and a script that fails to parse keeps the version already
running rather than dropping it.

## Roadmap
- [x] Lexer, pattern-matching parser and a suspendable interpreter
- [x] Variables, arithmetic, combined conditions and loops
- [x] Items, the inventory, entities, the world and targeting
- [x] Sixty events, from `on move` to `on server disconnect`
- [x] Text and maths expressions, and `wait until <condition>`
- [x] A real command tree with single-file reload and completion
- [x] Effect commands typed into chat, and the first config file
- [x] Functions, options, `is set` and `x if condition else y`
- [x] List variables, dynamic variable names and changers for game values
- [x] Item data, and tooltip lines, holograms and beams only you see
- [ ] Syntax highlighting for editors

## Contributing
Issues and pull requests are welcome. If you are reporting a bug, include the smallest script that reproduces it and
the error line the game printed, since that names the file and line the parser objected to.

If you use AI anywhere in a pull request, say exactly how.

Code follows [code-conventions.md](code-conventions.md), which is based on Skript's.

## Building
Requires JDK 25.

```
./gradlew build
```

The jar lands in `build/libs/`. `./gradlew runClient` starts a development client with the mod already loaded.

The language is deliberately isolated from Minecraft: only `game.MinecraftBridge`, `MineSkriptClient`,
`MineSkriptCommand` and `MineSkriptMessages` import anything from Minecraft or Fabric, and everything else is covered
by unit tests that run against a fake game. That means `./gradlew test` needs no game at all, and a port to a new
Minecraft version only touches the `game` package.

Syntax is organised in modules, like Skript's: `common` holds everything that never touches the game (text, maths,
lists, variables, loops), and `client` has one module per game feature (`client/inventory`, `client/chat`,
`client/world` and so on). Addons are modules too; see [ADDONS.md](ADDONS.md).

## Credits
Syntax patterns are modelled on [SkriptLang/Skript](https://github.com/SkriptLang/Skript), and the idea of scripting
the client at all comes from [Minescript](https://github.com/maxuser0/minescript).

Licensed under Apache-2.0 (see LICENSE).
