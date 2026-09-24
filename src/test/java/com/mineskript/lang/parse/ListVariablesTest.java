package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListVariablesTest {
    private final ScriptRunner runner = new ScriptRunner();

    private List<String> run(String body) {
        runner.run("on load:\n" + body.indent(4));
        return runner.game.messages;
    }

    @Test
    void setReplacesTheListWithIndicesFromOne() {
        assertEquals(List.of("a, b and c", "b", "3"), run("""
                set {list::old} to "gone"
                set {list::*} to "a", "b" and "c"
                send {list::*}
                send {list::2}
                send size of {list::*}
                """));
        assertEquals("a", runner.global("list::1"));
        assertFalse(runner.variables.global().containsKey("list::old"));
    }

    @Test
    void addAppendsAtTheLowestFreeNumberIndex() {
        assertEquals(List.of("a, c and d", "d", "c"), run("""
                set {l::*} to "a", "b" and "c"
                delete {l::2}
                add "d" to {l::*}
                send {l::*}
                send {l::2}
                add "e" to {_unused::*}
                send {l::3}
                """));
    }

    @Test
    void addingSeveralValuesAndAddingToAnEmptyList() {
        assertEquals(List.of("x, y and z", "3"), run("""
                add "x" to {_l::*}
                add "y" and "z" to {_l::*}
                send {_l::*}
                send amount of {_l::*}
                """));
    }

    @Test
    void removeTakesTheFirstEqualEntryAndRemoveAllTakesEvery() {
        assertEquals(List.of("b, a and b", "1, 3 and 4", "a"), run("""
                set {_l::*} to "a", "b", "a" and "b"
                remove "a" from {_l::*}
                send {_l::*}
                set {_i::*} to 1, 2, 1 and 1
                remove all 1 from {_i::*}
                set {_i::*} to 1, 2, 3 and 4
                remove 2 from {_i::*}
                send {_i::*}
                set {_l::*} to "a", "b" and "b"
                remove all "b" from {_l::*}
                send {_l::*}
                """));
    }

    @Test
    void deleteAndClearEmptyTheListIncludingNestedEntries() {
        assertEquals(List.of("0", "not set", "kept"), run("""
                set {l::*} to 1, 2 and 3
                set {l::a::b} to 4
                set {other} to "kept"
                clear {l::*}
                send size of {l::*}
                if {l::*} is not set:
                    send "not set"
                send {other}
                """));
        assertEquals(List.of("other"), List.copyOf(runner.variables.global().keySet()));
    }

    @Test
    void singleEntriesBehaveLikeVariables() {
        assertEquals(List.of("5", "<none>", "", "2"), run("""
                set {stats::kills} to 4
                add 1 to {stats::kills}
                send {stats::kills}
                delete {stats::kills}
                send {stats::kills}
                send "%{stats::*}%"
                set {stats::a} to 1
                set {stats::b} to 1
                send size of {stats::*}
                """));
    }

    @Test
    void everyScopeHasItsOwnLists() {
        assertEquals(List.of("g, r and l"), run("""
                set {x::*} to "g"
                set {-x::*} to "r"
                set {_x::*} to "l"
                send "%{x::1}%, %{-x::1}% and %{_x::1}%"
                """));
        assertEquals("r", runner.variables.ram().get("x::1"));
    }

    @Test
    void readingAListGivesOnlyItsDirectEntriesInInsertionOrder() {
        assertEquals(List.of("z, a and m"), run("""
                set {o::z} to "z"
                set {o::a} to "a"
                set {o::sub::deep} to "deep"
                set {o::m} to "m"
                set {o} to "the list itself"
                set {other::1} to "elsewhere"
                send {o::*}
                """));
    }

    @Test
    void dynamicNamesAreEvaluatedAndLowercased() {
        assertEquals(List.of("64", "64", "a1 b2", "3"), run("""
                set {home::%player%} to 64
                send {home::steve}
                send {HOME::%"STEVE"%}
                loop "a,b" split at ",":
                    set {count::%loop-value%} to "%loop-value%%loop-iteration%"
                send "%{count::a}% %{count::b}%"
                set {_i} to 3
                set {_x::%{_i}%} to {_i}
                send {_x::3}
                """));
        assertEquals(64.0, runner.global("home::steve"));
    }

    @Test
    void dynamicNamesWorkInsideTextAndSurviveComments() {
        assertEquals(List.of("home 64", "x"), run("""
                set {home::%player%} to 64 # the player's home
                send "home %{home::%player%}%"
                set {x::%"a#b"%} to "x" # a comment
                send {x::%"A#B"%}
                """));
    }

    @Test
    void tokenizerKeepsDynamicPartsWhole() {
        assertEquals(List.of(new Token("set", false), new Token("{my home::%Player's Name%}", false), new Token("to", false)),
                Tokenizer.tokenize("set {My   Home::%Player's Name%} to"));
        assertEquals(List.of(new Token("{_a::%{_b::%{_c}%}%}", false)), Tokenizer.tokenize("{_a::%{_b::%{_c}%}%}"));
        assertEquals(List.of(new Token("{x::%\"}\"%}", false)), Tokenizer.tokenize("{X::%\"}\"%}"));
    }

    @Test
    void dynamicNamesNest() {
        assertEquals(List.of("deep"), run("""
                set {_k} to "b"
                set {_a::b} to "x"
                set {_x::%{_a::%{_k}%}%} to "deep"
                send {_x::x}
                """));
    }

    @Test
    void unknownExpressionInAVariableNameIsAParseError() {
        assertEquals(List.of("t.ms:2: unknown expression \"+\" in variable name"),
                runner.errorsOf("on load:\n    set {x::%+%} to 1\n"));
        assertEquals(List.of("t.ms:2: loop-value is only available inside a loop"),
                runner.errorsOf("on load:\n    set {x::%loop-value%} to 1\n"));
    }

    @Test
    void loopingAListGivesValuesAndIndices() {
        assertEquals(List.of("alex=1", "steve=2", "3"), run("""
                set {homes::alex} to 1
                set {homes::steve} to 2
                loop {homes::*}:
                    send "%loop-index%=%loop-value%"
                    set {_last} to loop-iteration
                send "%{_last} + 1%"
                """));
    }

    @Test
    void loopIndexIsThePositionForOtherLoops() {
        assertEquals(List.of("1:a", "2:b", "1", "2"), run("""
                loop "a,b" split at ",":
                    send "%loop-index%:%loop-value%"
                loop 2 times:
                    send loop-index
                """));
    }

    @Test
    void loopIndexOutsideALoopOrInWhileIsAParseError() {
        assertEquals(List.of("t.ms:2: loop-index is only available inside a loop"), runner.errorsOf("on load:\n    send loop-index\n"));
        assertEquals(List.of("t.ms:3: loop-index is not available inside while"),
                runner.errorsOf("on load:\n    while 1 is 2:\n        send loop-index\n"));
    }

    @Test
    void membershipWorksWithListVariables() {
        assertEquals(List.of("in", "contains", "not in", "empty"), run("""
                set {_l::*} to "a" and "b"
                if "b" is in {_l::*}:
                    send "in"
                if {_l::*} contains "a":
                    send "contains"
                if "c" is not in {_l::*}:
                    send "not in"
                if {_l::*} doesn't contain "a":
                    send "wrong"
                if "a" is not in {_nothing::*}:
                    send "empty"
                """));
    }

    @Test
    void splitResultsFillAListVariable() {
        assertEquals(List.of("y", "3", "yes"), run("""
                set {_p::*} to "x,y,z" split at ","
                send {_p::2}
                send number of {_p::*}
                if "z" is in {_p::*}:
                    send "yes"
                """));
    }

    @Test
    void aSingleVariableHoldingAListKeepsWorking() {
        assertEquals(List.of("a, b and c", "3", "a and b"), run("""
                set {_s} to "a,b,c" split at ","
                send {_s}
                send size of {_s}
                set {_l::*} to "a" and "b"
                set {_copy} to {_l::*}
                send {_copy}
                """));
    }

    @Test
    void sizeDoesNotStealInventoryCountOrItemCount() {
        runner.game.slots[0] = new ItemValue("minecraft:stone", "Stone", 5, 0, 0);
        runner.game.slots[1] = new ItemValue("minecraft:stone", "Stone", 3, 0, 0);
        assertEquals(List.of("8", "8", "5", "5", "2"), run("""
                send number of stone in inventory
                send number of stone in the inventory
                send amount of held item
                send count of held item
                set {_l::*} to 1 and 2
                send amount of {_l::*}
                """));
    }

    @Test
    void sizeOfAWrittenListAndOfUnsetValues() {
        assertEquals(List.of("3", "0", "1"), run("""
                send size of 1, 2 and 3
                send size of {_nothing}
                set {_one} to "x"
                send the number of {_one}
                """));
    }
}
