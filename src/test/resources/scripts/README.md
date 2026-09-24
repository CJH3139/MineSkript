# MineSkript script tests

Every `.ms` file in this folder is a script test. `./gradlew test` runs them all through
`com.mineskript.scripttest.ScriptTests`, the same way skript-parser runs its test scripts. Each file shows up as its
own group in the test report, with one JUnit test for each `test` trigger in it, so a failure names the file, the test
and the line.

## Where a test goes

* `effects/`, `expressions/`, `conditions/` and `events/` hold tests for one syntax class each, and the file is named
  after that class: the test for `ExprLength.java` is `expressions/ExprLength.ms`. A file may also cover close
  relatives of its class (`ExprFloor.ms` also tests `ExprAbsoluteValue`); say so in the comment at the top.
* `general/` holds everything that is not one syntax class: sections, functions, options, variable names, text,
  arithmetic.
* When you change a syntax element, change its test to cover the new behaviour.

## Writing a test

* Indent with tabs, never spaces.
* Start each file with a `#` comment saying what it tests.
* A test is an event: `test "name":` (or just `test:`). The runner starts every test once, on its own, with fresh
  variables and a fresh fake game, so tests never depend on each other. Other triggers in the file (`on load:` and so
  on) are parsed but never run.
* Check things with `assert <condition> with "message"`. The condition is any condition you could write after `if`.
  The message says what went wrong, not what should happen: `"add 4 to 5 did not give 9"`, not `"add works"`. It can
  include values, such as `"a roll was %{_roll}%"`. `assert <condition>` on its own uses the message
  `assertion failed`.
* A failing assert does not stop the test: every failing assert is reported, each as `file:line: message`.
* The test also fails if it does not parse, ends with a script error that no `try` caught, shows an error in game, or
  runs out of steps. A file with no `test` trigger fails too.
* `wait` works and lets simulated time pass (`game time` goes up by one each tick). `wait until` checks its condition
  once per simulated tick and fails the test if it times out, just as it does in game.
* Test every form of the syntax: each spelling, the negated form, an unset value, an empty list.
* The game is `FakeGameBridge` with its default values (a world is loaded, the player is Steve at 0.5, 64, -3.5).

`test` and `assert` exist only in the tests. They are registered by `TestSyntax` on top of the normal syntax, so the
shipped mod and the generated documentation never see them.

## Example

```
# Tests for EffAdd: adding to numbers and to lists.

test "add to a number":
	set {n} to 5
	add 4 to {n}
	assert {n} is 9 with "add 4 to 5 did not give 9"
```
