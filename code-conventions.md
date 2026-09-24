# Code Conventions

These follow [Skript's code conventions](https://github.com/SkriptLang/Skript/blob/master/code-conventions.md),
adapted to a client-side Fabric mod. Where this file says nothing, do what the surrounding code does.

## Safety

MineSkript runs on players' own computers and acts as them on servers they join. Their trust matters more than any
feature.

### What scripts may do
A script may only do what the player could do by hand: press keys, click, look around, type in chat, read what the
client can see. Contributions must not:

* Give scripts abilities a player does not have, such as seeing through walls, reading server-only data, or
  sending packets the vanilla client would never send.
* Hide what a script is doing from the player running it.
* Send anything anywhere except to the server the player is connected to, and only through the normal client.
  MineSkript never contacts the internet on its own: no analytics, update checks or downloads.

### Files and I/O
Syntax must not expose the file system, the network or other programs (no "run command", "read file", "open
socket"). The only files MineSkript touches are its own: the `mineskript` folder, its scripts, `config.txt` and
`variables.json`.

Where the mod itself does I/O:
1. Close every stream with `try-with-resources`.
2. Never block the render thread for long; the game freezes while it waits.
3. Never lose the player's data: write to a temporary file and move it into place.

### Keeping the game responsive
Scripts run on the game's render thread. Anything a script does costs frame time.

* Never call `Thread.sleep` or block. Scripts pause with `wait`, which suspends the trigger and hands control back.
* Anything a user can loop must have a limit. Every trigger has a step budget and functions have a recursion cap;
  keep new constructs inside those limits.
* High-volume game hooks (sounds, particles, entities, chunks) must cost nothing while no script listens for them.
  Check `GameSignals.wants(...)` before building any values.

### Secrets and personal data
Never commit tokens, passwords, API keys or personal data. If you do, rotate the secret straight away: deleting it
from history later is not enough.

## Licensing

MineSkript is Apache-2.0. Contributed code is licensed under it, by you. Only include third-party code under a
compatible licence, and say where it came from in the pull request.

If you used AI to write any part of a pull request, say exactly how.

## Architecture

### Keep Minecraft out of the language
Only these may import Minecraft or Fabric classes:

* `game.MinecraftBridge` and the other classes in `game` that talk to the client
* `mixin.*`
* `MineSkriptClient`, `MineSkriptCommand` and `MineSkriptMessages`

Everything else (the lexer, parser, interpreter and every syntax element) goes through `GameBridge`. This is what
lets `./gradlew test` run without the game, and keeps a Minecraft update to the `game` package. `ArchitectureTest`
fails the build if anything else imports them.

To reach something new in the game, add a method to `GameBridge`, implement it in `MinecraftBridge`, and implement it
in the test `FakeGameBridge` so it can be tested.

### Modules
Syntax is organised in modules, the way Skript organises its own (`org.skriptlang.skript.common` and
`org.skriptlang.skript.bukkit`). A module implements `lang.module.SyntaxModule`: a `name()`, an optional `canLoad()`,
`register(SyntaxRegistry)` for its own elements and `children()` for its child modules. `DefaultSyntax` loads the
built-in modules; addons are modules too (see [ADDONS.md](ADDONS.md)).

* `common` (`com.mineskript.common`, `CommonModule`): everything that never touches the game: text, maths, lists,
  variables and changers, loops, comparisons, waiting and stopping, the periodic and script load events, and every
  event value. Nothing in `common` may import `com.mineskript.game`, Minecraft or Fabric, or call
  `context.world()` / `context.game()`. If an element needs the game, it does not belong in `common`.
* `client` (`com.mineskript.client`, `ClientModule`): one child module per game feature, each in
  `client/<feature>/<Feature>Module.java` with its elements, events included, in `client/<feature>/elements`:
  `movement`, `inventory`, `chat`, `hud`, `world`, `entity`, `player` and `server`. Put a new element in the
  feature a script writer would look for it in, and register it in that module only.

`common` loads first, because it defines the event values that the events of every other module declare.
`ArchitectureTest` checks the import rules above and `ModulesTest` checks that every element belongs to exactly one
module.

### Mixins
Prefer a Fabric API event or a public Minecraft method. When a mixin is the only way:

* Inject at `TAIL` of packet handlers, so the code runs once on the game thread after the packet has been applied.
* Keep the mixin tiny: turn the data into plain values and hand it to `GameSignals`. No logic in mixins.
* No anonymous or inner classes inside a mixin class; put helpers in the `game` package.
* The mixin config is `required: false` with `defaultRequire: 0`, so a mixin that fails to apply is skipped instead
  of crashing the game. Never change that.

### Syntax elements
One syntax element per class, the way Skript does it:

* Prefix the class: `Cond` (condition), `Eff` (effect), `Expr` (expression). Events are registered in the
  `<Feature>Events` class of their module.
* A condition's negated patterns live in the same class as its positive ones.
* Which module registers an element first must never decide how a line parses. When two patterns could match the same
  text, say which one wins with a `Priority` (`lang.parse.Priority`), like Skript's syntax priorities:
  * Conditions and effects are tried by priority, then in registration order. `SIMPLE` is the default;
    `COMBINED` is for patterns whose slots can swallow another element's words, such as `set %objects% to %objects%`
    or `%player% has %blocktype%`; `PATTERN_MATCHES_EVERYTHING` is for patterns that start with a bare slot taking
    any value, such as `%objects% is %objects%` or `%string% contains %string%`.
  * Expressions are tried by `Tier` (`SIMPLE`, `PROPERTY`, `COMBINED`) first and by priority within a tier.
  * `Priority.before(...)` and `Priority.after(...)` place an element just before or after a base priority when it
    must go between two groups. Add a test that shows which element wins.
  * A change in precedence changes how existing scripts parse: add a test that shows which element wins.
* Shared logic goes in a package-private helper class with no `register(SyntaxRegistry)` method.

### Documentation
Every syntax element documents itself; the documentation website is generated from it.

```java
@Name("Is Set")
@Description("Checks whether a value exists.")
@Examples({"on key press of \"g\":",
		"\tif {home} is set:",
		"\t\tsend \"you have a home\""})
@Since("1.0.0-alpha.4")
public final class CondIsSet implements Condition {
```

* `@Name`: short, Title Case.
* `@Description`: what it does and what a script writer must know (units, what happens with no value, when it
  fails). One string per paragraph.
* `@Examples`: complete scripts, one string per line, indented with `\t`, with `""` between separate scripts. They
  are parsed on every build, so they must work.
* `@Since`: the version it ships in.
* Events use the same fields in chained form: `registry.addEvent("Block Break", ...).description(...)
  .examples(...).since(...)`. So do built-in functions, registered with `registry.addFunction(...)` in the
  `<Feature>Functions` class of their module, the way Skript keeps its own in `DefaultFunctions`.
* Write for people who have never programmed. Plain words, no internal jargon.

`DocumentationTest` fails the build if anything is missing or an example does not parse.

## Code Style

### Formatting
* 4 spaces, no tabs, except inside `@Examples` strings.
* At most 120 characters per line.
* Imports sorted, no wildcards, no unused imports.
* Always use braces, even for one-line blocks.
* One blank line between methods; none at the start or end of a block.
* Each Java file ends with a single newline.

### Naming
* `UpperCamelCase` classes, `camelCase` fields and methods, `UPPER_SNAKE_CASE` constants.
* Descriptive names: `event`, `context`, `registry`, never `e`, `c`, `r`.
* Script-facing words (pattern text, messages) are lower case, like the language itself.

### Language features
* Java 25. Use records for plain data, sealed interfaces and pattern-matching `switch` where they make things
  clearer, and `var` sparingly.
* Classes are `final` unless they are designed to be extended.
* Fields are `final` whenever they can be.
* No `null` in script values: an unset value is `None.NONE`. Use `Optional` for "maybe" in Java APIs.
* Keep methods short. If a method needs a comment to explain each section, split it.

### Comments
* No comments in the code. Clear names, small methods and tests explain it instead.
* The one exception is Javadoc on the API addons use: `api`, `lang.module`, the documentation annotations in `doc`,
  the built-in function API in `lang.function` (`FunctionInfo`, `FunctionParameter`, `FunctionBody`), and the public
  methods of `SyntaxRegistry`, `EventInfo` and `Priority`.

### Errors
* Parse errors (`SyntaxException`) and runtime errors (`ScriptError`) are read by script writers. Say what went
  wrong in their terms and, when you can, how to fix it: "loop-value is only available inside a loop", not
  "illegal state".

## Tests

* Every new syntax element, event or behaviour needs a test. `./gradlew test` must pass before a pull request.
* Tests run against `FakeGameBridge`; never require the game.
* A bug fix comes with a test that fails without the fix.
* Something that can only be checked in game (a mixin, a Fabric hook) needs a note in the pull request saying how
  you tested it.
