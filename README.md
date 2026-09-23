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
> MineSkript is in **early release**. The language now has variables, arithmetic, combined conditions, loops, items,
> entities and polled events, but not yet functions or list variables, and syntax may still change between versions.
> What is listed below works today.

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
`.minecraft/mineskript/` and writes an `example.ms` into it. The example is written once, when that folder is first
created, so deleting it is permanent and a reload never puts it back.

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

Two things worth knowing before you write your first script. Do not put `make player say` inside `on chat`: the server
sends that message back, `on chat` fires again, and it loops forever, whereas `send` stays on your client and is safe.
And do not use `player` or any of its properties inside `on load`: it runs at the title screen where there is no player
yet, and the script will stop with `no world`.

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
| `on move:` | the player's block position changes; `event-from x`, `event-to x` and the y and z forms |
| `on jump:` / `on land:` | leaving and touching the ground; `on land` gives `event-fall distance` |
| `on sneak:` / `on stop sneaking:` | also `on sprint:` and `on stop sprinting:` |
| `on damage:` / `on heal:` | health fell or rose; `event-damage` and `event-healed` |
| `on death:` / `on respawn:` | health reached zero, and came back |
| `on hunger change:` | `event-hunger change` |
| `on held item change:` | the hotbar selection moved; `event-previous item` and `event-item` |
| `on inventory change:` | any slot's item or count changed; `event-item` |
| `on start using item:` / `on stop using item:` | `event-item` |
| `on level change:` | `event-level change` is how many levels were gained or lost, not the new level |
| `on gamemode change:` | `event-gamemode` |
| `on weather change:` | rain or thunder started or stopped |
| `on screen open:` / `on screen close:` | a GUI opened or closed |
| `on world join:` / `on world leave:` | |
| `on consume:` | a food or drink use ran to completion; `event-item` is what was eaten |
| `on item break:` | the held tool broke; `event-item` is the tool as it last was |
| `on xp change:` | experience points moved without the level changing; `event-xp change` is the delta |
| `on effect gain:` / `on effect lose:` | `event-effect` is the bare effect name, the same spelling `player has effect` takes, and `on effect gain` also gives `event-effect level` |
| `on mount:` / `on dismount:` | `event-entity` is the vehicle, the one just left in the case of `on dismount` |
| `on dimension change:` | `event-from dimension` and `event-to dimension` |
| `on player join:` / `on player leave:` | a name appeared in or left the tab list; `event-player` |
| `on chat send:` | you sent a chat line; `message` holds it |
| `on command send:` | you sent a command; `message` holds it without the leading slash |

Key names are `"r"`, `"space"`, `"f6"`, `"left shift"`, `"mouse left"` and the like.

Everything from `on move` down is polled once per tick, so those events fire on the tick after the change, and the
first tick in a world sets the baseline without firing anything except `on world join`. `event-fall distance` is the
larger of the readings either side of the landing, since the game zeroes it the moment you touch the ground. An
`event-` value is only available inside the event that provides it; using one elsewhere is a parse error naming the
line. `on world leave` runs after the world is already gone, so the expressions and effects that read the world are
unavailable inside it and `send` lands in the log rather than your chat; it is there for saving variables.

`on consume` keys on a consumable use that ran to completion, not on a hunger change, because hunger arrives from the
server a tick or two later. Releasing the use early fires nothing. `on item break` keys on a held tool with at most one
use left becoming air in the same slot, so scrolling away from a nearly broken tool is not a break. The client cannot
tell a break apart from any other way a nearly worn tool can leave that slot, so dropping or trading one away fires
`on item break` as well. `on chat send` and `on command send` are the only two events here that are not polled; they
come from a real callback, so they fire on the same tick you press enter. Neither one can fire itself: chatting from
inside `on chat send` does not run it again, and the same holds for `on command send`, but the two guards are separate,
so a command sent from inside `on chat send` does reach `on command send`. `on player join` and `on player leave` read
the tab list, which is the server's view and is empty on a single player world. The first time a tab list goes from
empty to populated after you join a world is taken as a fresh baseline rather than as a join for every name on it, so a
roster that arrives a tick after you connect does not fill your chat, and the price is that the first player to join a
genuinely empty server is recorded without firing. Only that first arrival is a baseline: a tab list that empties and
fills again later in the same world does fire a join for every name that comes back. Crossing a portal on a server that swaps the world outright arrives as `on world leave` then
`on world join` rather than `on dimension change`.

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
| `select slot <number>` | pick a hotbar slot; anything outside 0 to 8 is clamped into it |
| `swap hands` | |
| `drop item` / `drop the whole stack` | |
| `look at <x>, <y>, <z>` | point the camera at a position |
| `set yaw to <number>` / `set pitch to <number>` | |
| `show title <text>` / `show subtitle <text>` / `show action bar <text>` | |
| `play sound <text>` | client-side only, nobody else hears it |
| `close screen` | |
| `wait until <condition>` | parks this trigger until the condition is true, giving up after 30 seconds with an error |
| `send command "/spawn"` | the same as `execute command` |
| `open inventory` | |
| `copy <value> to clipboard` | |
| `take screenshot` | the same picture F2 takes |
| `disconnect` | leave the server or the world; it ends the trigger run as `stop` does |

`wait until` tests its condition immediately, so an already true condition costs nothing, and then once per tick after
that. Other triggers keep running while it waits, and each fire of an event parks its own independent frame. After 30
seconds it stops that run and prints an error naming the line, so a script can never park forever.

**Conditions**

```
if <value> is <value>:              also is not, isn't, =, !=
if <number> is less than <number>:  also greater than, above, below, <, >, <=, >=,
                                    less than or equal to, greater than or equal to,
                                    is at least <n>, is at most <n>,
                                    is between <a> and <b>
if block below player is stone or dirt:
if "Steve" is in online player names:
                                    also is not in, isn't in; and the other way
                                    round, online player names contains "Steve",
                                    also does not contain, doesn't contain
if player is sneaking:              also on ground, sprinting, and the "is not" forms
if key "shift" is held:
if player has diamond:              also player is holding diamond
if inventory is full:               also is empty; slot 3 is empty
if player is in water:              also in lava, on fire, flying, sleeping,
                                    blocking, using an item, swimming, invisible
if it is raining:                   also it is thundering
if gamemode is "creative":
if nearest entity is within 5 blocks:
if "minecraft" contains "craft":    also starts with, ends with, and the "does not" forms
if "Hello" is "hello" ignoring case:
if player has effect "speed":       also doesn't have effect
if player is riding:                also is not riding
if player can see sky:              also sees sky, cannot see sky, does not see sky
if block at 10, 64, 10 is stone:
else if <condition>:
else:
```

`is in` and `contains` ask whether one value appears among the items of a list, which is what you want for
`online player names`. A list on the right of `is` still reads the older way: every item has to match, unless the items
are joined with `or`, in which case any one of them does. Either way an empty list matches nothing, so a comparison or a
membership test against an empty tab list is false rather than quietly true.

Conditions can also be combined with `and` and `or`; see Combining conditions below.

**Values**

```
player                          you
message                         the chat line, inside on chat, on chat send and on command send
block below player              also above, north, south, east, west, at; block 2 below player
health of player                also player's health, max health, hunger, name
player's y-coordinate           x, y and z; also "y coordinate", "y-coord"
"text with %player's health%"   expressions inside %...%, %% for a literal percent sign
42, 1.5, -3                     numbers
5 ticks, 2 seconds, 1 minute    timespans, 20 ticks per second
stone, oak log                  block types
stone, dirt or grass block      lists, on the right of a comparison
random number between 1 and 6   a real number, the low end included, the high end not
random integer between 1 and 6  a whole number, both ends included
```

Use `random integer` for a die roll or any other whole-number draw. `random number` gives you the full spread of
decimals between the two bounds, which is rarely what a script wants on its own.

**Items and the inventory**

```
held item                       also item in hand, tool
offhand item                    also item in offhand
item in slot 5                  0 to 8 is the hotbar, 0 to 40 is everything
name of held item               also id of, count of, damage of, max damage of
number of stone in inventory
selected slot                   also free slots, used slots
```

An item compares against a block type by id, so `if held item is diamond pickaxe:` works. An empty hand reads as
`air` with a count of 0, never `<none>`, so `if held item is air:` is the way to test for one and a comparison is
always safe. On its own, `air` is the breath value below, not an item.

`free slots`, `used slots`, `inventory is full` and `inventory is empty` count the 36 main slots only, so a player
in full armour with something in the offhand and nothing anywhere else reads as empty.

**Text and maths**

```
uppercase "hello"               also lowercase
length of "hello"               also "hello"'s length
"minecraft" from character 1 to 4
"a b b" with "b" replaced with "c"
"hello" joined with " world"
first 3 characters of "minecraft"        also last
round 2.5                       also floor, ceiling, absolute value of, square root of
3.14159 rounded to 2 places
minimum of 3 and 8              also maximum of
random number between 5 and 10  also random integer
```

A quoted string cannot sit inside a `%...%` interpolation, because the language has no escape syntax and the quote ends
the surrounding text. Put the value in a variable first and interpolate the variable.

**The client's environment**

```
biome
light level                     also sky light
server address                  also server brand, ping, fps; these four read with no world,
                                and server address is singleplayer off a server
saturation                      also total xp
vehicle                         the entity you are riding, or <none>
online player names             a list, from the tab list
level of effect "speed"         0 when the effect is absent
block at 10, 64, 10
x coordinate of target block    also y and z
```

Nothing here reads anything the player cannot already see on their own screen. `x coordinate of target block` needs a
block under the crosshair and raises `there is no target block` when there is none, so test `target block` first if you
might be looking at the sky.

**The player, the world and what you are looking at**

```
gamemode                        survival, creative, adventure, spectator
xp level                        also xp progress
air                             also max air, armor
yaw                             also pitch, speed, fall distance
dimension
game time                       also players online, difficulty
target block                    what the crosshair is on; air when nothing
target entity                   also nearest entity, nearest player
name of nearest entity          also id of, distance of, x coordinate of
```

An entity expression reads as `<none>` when there is nothing there, so check it before reading a property from it.
With nothing there, `nearest entity is within 5 blocks` is false and `nearest entity is not within 5 blocks` is true.
An entity also compares against text by its id, so `if target entity is "zombie":` and
`if nearest entity is "minecraft:zombie":` both work.

**Variables**

```
{count}                         global, saved to .minecraft/mineskript/variables.json
{-count}                        global, this session only
{_count}                        local to one trigger run
```

Names are case-insensitive and may contain spaces. A variable goes anywhere a value goes; an unset one reads as `<none>`.

```
set {x} to <value>
add 1 to {x}                    also increase {x} by 1; an unset variable counts as 0
remove 1 from {x}               also subtract, decrease {x} by 1
delete {x}                      also clear
```

**Arithmetic**

```
{x} + 1     {a} - {b}     {x} * 2     {x} / 4     2 ^ 8     ({a} + {b}) * 2
```

Standard precedence, parentheses group, numbers only. Operators need spaces around them: `{x} - 3` subtracts, `-3` is a
negative number.

Arithmetic is strict about unset variables: `{x} + 1` raises `variable is not set`, while `add 1 to {x}` counts an
unset variable as 0. Use `add` to build a counter up from nothing.

**Combining conditions**

```
if player is sneaking and key "w" is held:
if {hp} is less than 6 or {food} is less than 6:
if (player is sneaking or player is sprinting) and player is on ground:

if all:                         also if any, else if all, else if any
    player is sneaking
    key "w" is held
then:
    send "sneak-walking"
```

`and` binds tighter than `or`. A line that already reads as one condition, such as `is stone or dirt`, is never split.

**Loops**

```
loop 5 times:                   also loop {n} times, once, twice, thrice
loop stone, dirt and gravel:    any list, or a variable holding one
while player is sneaking:

    loop-value                  the current item; in a times loop, the number; not available inside while
    loop-iteration              1-based pass count; also loop-counter
    continue                    next pass
    exit loop                   leave the innermost loop; also stop loop
```

`wait` inside a loop works. A loop that never waits and never ends is stopped after 10,000 statements with an error
naming the line.

## Commands
`/mineskript` is the whole tree and `/ms` is the same tree, registered as a Brigadier redirect rather than a second
copy, so the two can never drift apart.

```
/ms help                 this list
/ms reload               reload every script
/ms reload scripts       the same thing, spelled out
/ms reload <file.ms>     reload one script and leave the others running
/ms reload variables     re-read variables.json from disk
/ms reload all           variables and every script
/ms list                 one line per loaded script, with its trigger count and its errors if it has any
/ms errors               the errors from the last load, again, for when chat has scrolled
/ms info                 version, script folder, script and trigger counts, ticks since the last full reload
```

`/ms` claims five words of its own: `help`, `reload`, `list`, `errors` and `info`. Anything else you type after it,
such as a server's own `/ms warp home`, is passed straight to the server rather than answered with a client-side
error. Those five are the only things the alias takes from you, so a server whose own `/ms` has a `list` or an `info`
loses that one to MineSkript while the rest of its tree still reaches it. `/mineskript` is always ours whatever the
server does.

`<file.ms>` completes from the `.ms` files actually in the folder and from the scripts still loaded, so a script whose
file you have deleted can be unloaded by tab completion instead of typed blind. A name that is not there is an error
naming the folder rather than a silent no-op, and a name that is not a `.ms` file at all, such as `notes.txt`, is
refused and told why instead of being looked up. A file name containing a space cannot be typed, because the command
reads one unquoted word, so such a file is not offered as a completion either. A name typed in the wrong case is
resolved to the spelling on disk before anything else happens, so `mining.ms` reloads `Mining.ms` rather than loading a
second copy of it beside the first.

A single-file reload re-parses that one file, swaps its triggers in place, drops only that file's waiting frames, and
fires `on load` for that file alone. The tick counter, the key state, the held inputs and every variable are left
exactly as they were. A frame of that file which happens to be running at the moment of the reload finishes the work it
is already doing and is then discarded rather than resumed. A frame belonging to any other file is untouched and
resumes on the tick it always would have.

If the new text does not parse, the version already running stays running and the message says "did not reload", so a
typo never takes a working script off the air. When the file that is kept has no triggers at all, the second line says
nothing from it is running rather than calling the running version unchanged. A file that is gone from the folder is
unloaded and a file that is new since the last full load is added, so neither adding nor deleting a script needs a full
reload. A reload a script sets off with `send command` while a reload is still running is ignored with a warning
instead of starting a second one.

The first line of everything the mod prints carries a `MineSkript` prefix in aqua, and the rest of that block is
indented to line up underneath it, so ten lines of help read as one answer rather than ten. Green means success, yellow
a warning and red an error. A reload says what it is about to do, then the outcome with the time it took in
milliseconds, and every outcome carries that time, including a name that was missing and a name that was refused. A
load with errors prints the count, then each error as `file:line: message` with the source line itself indented in grey
underneath. That source line is the text the parser actually read, captured when the parse failed, so `/ms errors`
still shows what went wrong after you have edited the file. `/ms list` counts the same errors `/ms errors` lists, so a
file whose latest text does not parse is marked in `/ms list` even while the working version it kept stays on the air.
`/ms reload variables` says whether the file was really re-read rather than reporting success over the top of its own
warning. Errors raised while a script runs carry the same prefix and the same red and stop only that one trigger
run.

## Roadmap
- [x] Lexer, pattern-matching parser and a suspendable interpreter
- [x] Events, effects, conditions and expressions listed above
- [x] Script loading, `/mineskript reload` and per-line error reporting
- [x] Verified in game on a real client
- [x] Variables in three scopes, saved to disk
- [x] Arithmetic
- [x] `and` / `or` between conditions, inline and as `if all` / `if any` sections
- [x] `loop N times`, `loop <list>`, `while`, `continue`, `exit loop`
- [x] Items, the inventory and item properties
- [x] Player, world and targeting values
- [x] Thirty-three polled events, from `on move` to `on player leave`
- [x] Slot, rotation, HUD and sound effects
- [x] `on chat send` and `on command send`, straight from the send callback
- [x] Text and maths expressions
- [x] The client's own environment: biome, light, server, effects and the targeted block
- [x] `wait until <condition>`, and the clipboard, screenshot, inventory and disconnect effects
- [x] A real command tree: `/ms`, single-file reload, completion, and one message shape for everything the mod prints
- [ ] Functions
- [ ] List variables and dynamic variable names
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

The language is deliberately isolated from Minecraft: only `game.MinecraftBridge`, `MineSkriptClient`,
`MineSkriptCommand` and `MineSkriptMessages` import anything from Minecraft or Fabric, and everything else is covered
by unit tests that run against a fake game. That means `./gradlew test` needs no game at all, and a port to a new
Minecraft version only touches the `game` package.

## Credits
Syntax patterns are modelled on [SkriptLang/Skript](https://github.com/SkriptLang/Skript), and the idea of scripting the
client at all comes from [Minescript](https://github.com/maxuser0/minescript).

Licensed under Apache-2.0 (see LICENSE).
