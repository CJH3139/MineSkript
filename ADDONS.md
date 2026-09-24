# Writing a MineSkript addon

An addon is a separate Fabric mod that adds syntax (events, conditions, effects, expressions and built-in functions)
to MineSkript.
Players install it next to MineSkript, and its syntax works in script files and in effect commands like the built-in
syntax does.

An addon runs with the same trust as MineSkript itself, so it must follow the safety rules in
[code-conventions.md](code-conventions.md): scripts may only do what the player could do by hand, and syntax must not
reach the file system, the network or other programs.

## 1. Depend on MineSkript

Put the MineSkript jar on your compile classpath (for example `modCompileOnly files("libs/mineskript-<version>.jar")`
in `build.gradle`) and declare the dependency in your `fabric.mod.json`, together with the `mineskript` entrypoint
that names your addon class:

```json
{
  "schemaVersion": 1,
  "id": "example-addon",
  "version": "1.0.0",
  "environment": "client",
  "entrypoints": {
    "mineskript": ["com.example.addon.ExampleAddon"]
  },
  "depends": {
    "mineskript": ">=1.0.0-alpha.8"
  }
}
```

## 2. Implement `MineSkriptAddon`

An addon is a module, organised the same way as MineSkript's own syntax (see "Modules" below): `name()`, and
`register(SyntaxRegistry)` for its elements.

```java
package com.example.addon;

import com.mineskript.api.MineSkriptAddon;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class ExampleAddon implements MineSkriptAddon {
    @Override
    public String name() {
        return "Example Addon";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        registry.addEventValue(EventValue.of("ping count", SkType.NUMBER, "How many pings there have been."));
        registry.addEvent("Ping", (match, scope) -> Optional.of(new Event.State("example:ping")), "on ping")
                .values("ping count")
                .description("Fires when the example addon pings.")
                .examples("on ping:", "\tshout \"ping %event-ping count%\"")
                .since("1.0.0");
        EffShout.register(registry);
        ExprAnswer.register(registry);
    }
}
```

`name()` is what `/ms info` lists and what the generated documentation shows in each element's `addon` field. It is
also the addon's module path, shown in each element's `module` field.

### Modules

MineSkript's own syntax is split into modules, like Skript's: `common` for everything that never touches the game
(text, maths, lists, variables, comparisons, loops) and `client` with one child module per game feature
(`client/movement`, `client/inventory`, `client/chat`, `client/hud`, `client/world`, `client/entity`,
`client/player`, `client/server`). A module implements `com.mineskript.lang.module.SyntaxModule`, and
`MineSkriptAddon` extends it, so a larger addon can split its syntax the same way by returning child modules:

```java
@Override
public List<SyntaxModule> children() {
    return List.of(new PingModule(), new WeatherModule());
}
```

A child module registers after its parent, under the path `Example Addon/<child name>`. Override `canLoad()` to
return false to leave a module (and its children) out, for example when another mod it needs is missing; an addon whose
`canLoad()` is false is skipped without an error. `HierarchicalModule` is a ready-made module that only groups
children.

## 3. Write the syntax elements

Follow the same rules as MineSkript's own syntax: one element per class, prefixed `Eff`, `Cond` or `Expr`, with a
static `register(SyntaxRegistry)` method and the documentation annotations from `com.mineskript.doc`.

```java
package com.example.addon;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

@Name("Shout")
@Description("Shows a message to you in capital letters. Only you see it.")
@Examples({"on load:",
        "\tshout \"hello\""})
@Since("1.0.0")
public final class EffShout implements Statement {
    private final int line;
    private final Expression text;

    private EffShout(int line, Expression text) {
        this.line = line;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffShout(scope.line(), match.slot(0))), "shout %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        String message = Converters.toText(text.evaluate(context), context);
        context.game().showMessage(message.toUpperCase(Locale.ROOT));
        return Flow.CONTINUE;
    }
}
```

This example, with `ExprAnswer` (an expression that returns 42), is kept as a working test in
`src/test/java/com/mineskript/api/example`, so it always compiles against the current API.

### Documentation annotations

* `@Name`: a short Title Case name.
* `@Description`: what the element does, for people who have never programmed. One string per paragraph.
* `@Examples`: complete scripts, one string per line, indented with `\t`, `""` between separate scripts.
* `@Since`: your addon's version that first had the element.
* `@Keywords`, `@Events` (optional), and `@NoDoc` to leave a helper element out.
* Events are documented with the chained calls on `addEvent(...)`: `.description(...)`, `.examples(...)`,
  `.since(...)`, `.values(...)` for the event values it provides and `.cancellable()`.

### Patterns

Patterns use Skript's notation: `[optional]`, `(a|b)` choices, `tag:` on a choice to read it back with
`match.has("tag")`, and `%type%` slots such as `%string%`, `%number%`, `%objects%` or `%-number%` for an optional
slot. `%condition%` takes a whole condition (anything that can follow `if`) and fills its slot with a boolean
expression. Return `Optional.empty()` from a factory to let the next pattern or element try; throw
`SyntaxException` to make the line a parse error with your message.

### Events and event values

An addon event creates an `Event.State` with a name of its own (prefix it with your addon id, as in `example:ping`).
Fire it from your mod with `GameSignals.emit("example:ping", Map.of("ping count", 3.0))`: MineSkript runs the
triggers on the next client tick. `emit` does nothing while no loaded script listens for that event, so calling it
often costs nothing. Define each event value once with `addEventValue` before the events that provide it (in a
parent module if several child modules use it); MineSkript adds the `event-<name>` expression for it after your
addon has loaded.

### Built-in functions

Built-in functions are Skript's "Java functions", such as `round(n, d)` or `location(x, y, z)`: scripts call them as
`name(arguments)` anywhere a value can go, or on a line of their own. Register one with `addFunction` and document it
with the chained calls, the same way events are documented:

```java
registry.addFunction("twice", SkType.NUMBER, (arguments, context) -> 2 * (Double) arguments.get(0),
                FunctionParameter.of("n", SkType.NUMBER))
        .description("Doubles a number.")
        .examples("on load:", "\tsend \"%twice(21)%\"")
        .since("1.0.0");
```

* The body (`com.mineskript.lang.function.FunctionBody`) gets one value per parameter, already converted to the
  parameter's type: a `Double` for a number, a `String` for text, a `Location` for a location, and so on. Return a
  value of the return type, or `None.NONE` for no value. Throw `ScriptError` to stop the line with a message.
* Parameters come from `FunctionParameter`: `of(name, type)` for a required value, `optional(name, type, value)` for
  one with a default (`optional(name, type, shown, context -> ...)` works the default out when the function is
  called, such as the dimension you are in), and `list(name, type)` for any number of values, which the body gets as
  a non-empty `List`. When a list parameter is the only one, every argument goes into it, so `max(1, {l::*}, 3)`
  works. Parameters with a default come last.
* Like Skript's simple Java functions, a call gives no value without running the body when an argument has no value
  or a list argument is empty.
* `.returnsList()` makes a function give one value per value of its list parameter, like Skript's `clamp`.
* Function names ignore case (`isNaN` is also `isnan`) and must be unique: registering a name that is already taken
  throws, and a script that defines a function with a built-in name gets a parse error. Choose names that are
  unlikely to clash with other addons or with functions script writers already have.
* The body runs on the game thread like the rest of a script, so keep it quick. Anything that reads the game goes
  through `context.game()`.

Functions appear in the generated documentation in the top-level `functions` array, with their signature (such as
`twice(n: number) :: number`), parameters, return type, `addon` and `module`.

## Priorities and registration order

* MineSkript loads all of its own modules first, then each addon in the order Fabric lists the `mineskript`
  entrypoints, which you cannot control.
* When a line could match more than one condition or effect, they are tried by `Priority`, then in registration
  order. Give a catch-all pattern a later priority so it never hides a specific one:
  `registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING, factory, "%objects% is %objects%")` for a pattern that
  starts with a bare slot, `Priority.COMBINED` for one whose slots can swallow another element's words (such as
  `set %objects% to %objects%`), and the default `Priority.SIMPLE` for everything else. `Priority.before(...)` and
  `Priority.after(...)` place an element just before or after one of these.
* Expressions are tried tier by tier (`Tier.SIMPLE`, then `PROPERTY`, then `COMBINED`), and by priority, then
  registration order, within a tier (`addExpression(type, tier, priority, factory, patterns...)`).
* Within one priority, the element registered first wins, so a built-in pattern beats an addon pattern of the same
  priority that matches the same text, and an earlier addon beats a later one. Choose words that do not collide with
  built-in syntax or other addons.
* An expression pattern must need at least one word or a second value besides its only slot (so `%number%` alone,
  or `[the] %number%`, is refused), because such a pattern would make parsing exponential.
* `register` runs once, while the game is starting and before any world exists. Do not touch the game there.

## When something goes wrong

If loading the addon throws, in its own `register` or in any of its child modules (including a missing class),
MineSkript logs the error with the addon's name and starts without that addon: everything the addon registered before
throwing is discarded, and other addons still load.

## Code outside syntax

Keep Minecraft classes out of your syntax elements and go through `context.game()` (the `GameBridge`) like
MineSkript's own syntax does, so you can test your addon with plain JUnit tests the way MineSkript tests itself.
