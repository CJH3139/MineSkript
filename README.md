# MineSkript
**MineSkript** is a client-side Fabric mod where you write what you want your client to do in plain English, using a
scripting language of its own design. Scripts are text files you drop in a folder, they reload without restarting the
game, and they react to ticks, key presses and chat.

The language is modelled on [Skript](https://github.com/SkriptLang/Skript) and borrows its syntax patterns wherever an
equivalent exists, so `wait 5 seconds`, `if health of player is less than 6` and `send "hello"` all mean what a Skript
user would expect. The interpreter is written from scratch in Java rather than embedding Python or Lua, which is what
lets a script pause mid-line without blocking the game.

Everything runs on your own client. There is no server component, nothing is installed on servers you join, and the
mod only ever does what the player could do by hand.

> [!IMPORTANT]
> MineSkript is in **early release**. The language is deliberately small for now, with no variables, loops or functions
> yet, and syntax may still change between versions. What is listed below works today.

## Why a custom language?
Modding a client normally means Java, mappings, a build system and an API surface to learn before you can make the game
do anything at all. MineSkript replaces all of that with near-English syntax in a text file: `if block below player is
stone:` does what it reads like, and you press reload instead of recompiling. The barrier to entry is a text editor.

MineSkript can stop a script in the middle and pick it up later, which is the thing
[Minescript](https://github.com/maxuser0/minescript) cannot. Python has no way to freeze a half-finished statement, so
scripts there live on their own threads and every game call has to hop back to the render thread, landing a tick late
against a world that may already have moved.

Because the interpreter is ours, a trigger runs on the render thread itself. `wait 20 ticks` sets the execution aside
and hands control straight back to the game; twenty ticks later the next line continues from exactly where it stopped,
reading the blocks and keys as they really are at that moment. Nothing is threaded, nothing is queued, nothing is
stale.

The cost is that the language only does what has been built into it. That trade is the whole point: correctness and
readability over generality.

In short: Minescript asks you to know Python and accept a tick of latency on every call, MineSkript asks you to write a
sentence and gives you the exact game state.

## Requirements
- Minecraft **26.2**
- **Fabric Loader** 0.19.5 or newer
- **Fabric API** 0.160.0+26.2
- A **Java 25** runtime

## Download
No release is published yet, so build it yourself with the instructions under [Building](#building); the jar lands in
`build/libs/`.

## Getting Started
Drop the jar and Fabric API in your `mods` folder and launch the game once. MineSkript creates
`.minecraft/mineskript/` and writes an `example.ms` into it.

Scripts are plain text files ending in `.ms`. Every file in that folder loads at startup and again whenever you run
`/mineskript reload`, so you can edit a script and see the change without leaving the world.

```
on key press of "r":
    if block below player is stone or cobblestone:
        send "on stone, mining at y %player's y-coordinate%"
        hold attack
        wait 20 ticks
        release attack
    else:
        send "not stone: %block below player%"

every 5 seconds:
    if health of player is less than 6:
        make player say "low hp!"
```

Indent with tabs or spaces, but not both in one file. `#` starts a comment except inside quotes. The top level holds
only events, and every event line ends with `:`. Words are case-insensitive outside quotes.

## Syntax
Everything the language currently understands. Anything not listed here does not exist yet.

**Events**

| Syntax | Fires |
|---|---|
| `every <timespan>:` | on a fixed cadence, e.g. `every tick`, `every 2 seconds`, `every 500 milliseconds` |
| `on load:` | at startup and after every reload, the only event that runs with no world |
| `on chat:` | on any chat or system line the client receives; `message` holds the text |
| `on key press of "<key>":` | when a key goes down |
| `on key release of "<key>":` | when a key comes up |

Key names are `"r"`, `"space"`, `"f6"`, `"left shift"`, `"mouse left"` and the like.

**Effects**

| Syntax | Does |
|---|---|
| `wait <timespan>` | pauses this trigger; other triggers keep running |
| `send <value>` | prints to your own chat, visible only to you |
| `make player say "<text>"` | sends real chat as you |
| `execute command "/home"` | runs a command as you; the slash is optional |
| `stop` | ends this trigger run |
| `click attack` / `click use` | one press |
| `hold attack` / `release attack` | also `use` |
| `press key "w"` / `hold key "w"` / `release key "w"` | any key by name |

**Conditions**

```
if <value> is <value>:              also is not, isn't, =, !=
if <number> is less than <number>:  also greater than, above, below, <, >, <=, >=,
                                    less than or equal to, greater than or equal to,
                                    is between <a> and <b>
if block below player is stone or dirt:
if player is sneaking:              also on ground, sprinting, and the "is not" forms
if key "shift" is held:
else if <condition>:
else:
```

Nest `if` sections rather than combining conditions with `and` or `or`.

**Values**

```
player                          you
message                         the chat line, inside on chat
block below player              also above, north, south, east, west, at; block 2 below player
health of player                also player's health, max health, hunger, name
player's y-coordinate           x, y and z; also "y coordinate", "y-coord"
"text with %player's health%"   expressions inside %...%, %% for a literal percent sign
42, 1.5, -3                     numbers
5 ticks, 2 seconds, 1 minute    timespans, 20 ticks per second
stone, oak log                  block types
stone, dirt or grass block      lists, on the right of a comparison
```

## Commands
- `/mineskript reload` reloads every script and prints a summary plus each error as `file:line: message`.
- `/mineskript list` shows what loaded.

Errors raised while a script runs appear in red as `mineskript: file:line: message` and stop only that one trigger run.

## Roadmap
- [x] Lexer, pattern-matching parser and a suspendable interpreter
- [x] Events, effects, conditions and expressions listed above
- [x] Script loading, `/mineskript reload` and per-line error reporting
- [x] Verified in game on a real client
- [ ] Variables and `set`
- [ ] `while` and looping over lists
- [ ] Functions
- [ ] `and` / `or` between conditions
- [ ] Entities and inventory
- [ ] Syntax highlighting for editors

## Contributing
Issues and pull requests are welcome. If you are reporting a bug, include the smallest script that reproduces it and
the error line the game printed, since that names the file and line the parser objected to.

If you use AI anywhere in a pull request, say exactly how.

## Building
Requires JDK 25.

```
./gradlew build
```

The jar lands in `build/libs/`. `./gradlew runClient` starts a development client with the mod already loaded.

The language is deliberately isolated from Minecraft: only `game.MinecraftBridge`, `MineSkriptClient` and
`MineSkriptCommand` import anything from Minecraft or Fabric, and everything else is covered by unit tests that run
against a fake game. That means `./gradlew test` needs no game at all, and a port to a new Minecraft version only
touches the `game` package.

## Credits
Syntax patterns are modelled on [SkriptLang/Skript](https://github.com/SkriptLang/Skript), and the idea of scripting the
client at all comes from [Minescript](https://github.com/maxuser0/minescript).

Licensed under Apache-2.0 (see LICENSE).
