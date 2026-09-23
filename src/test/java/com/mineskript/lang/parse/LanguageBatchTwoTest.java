package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import java.util.List;
import org.junit.jupiter.api.Test;

class LanguageBatchTwoTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void textParsedAsNumber() {
        runner.run("on load:\n    set {t} to \"41\"\n    send \"%({t} parsed as number) + 1%\"\n    set {a} to \"abc\"\n    set {b} to \"2.5\"\n    send \"%{a} parsed as number%\"\n    send \"%{b} parsed as integer%\"\n");
        assertEquals(List.of("42", "<none>", "<none>"), runner.game.messages);
    }

    @Test
    void notPrefixNegatesAnyCondition() {
        runner.run("on load:\n    if not player is sneaking:\n        send \"standing\"\n    if not 1 is 1:\n        send \"wrong\"\n    if not 1 is 2 and 3 is 3:\n        send \"both\"\n");
        assertEquals(List.of("standing", "both"), runner.game.messages);
    }

    @Test
    void booleanLiteralsAndTruthyConditions() {
        runner.run("on load:\n    set {on} to true\n    set {off} to false\n    if {on}:\n        send \"on\"\n    if {off}:\n        send \"wrong\"\n    if {on} is true:\n        send \"is true\"\n    send \"%{on}% %{off}%\"\n");
        assertEquals(List.of("on", "is true", "true false"), runner.game.messages);
    }

    @Test
    void comparisonsChain() {
        runner.run("on load:\n    set {x} to 3\n    if 1 < {x} < 5:\n        send \"inside\"\n    if 1 < {x} <= 2:\n        send \"wrong\"\n");
        assertEquals(List.of("inside"), runner.game.messages);
    }

    @Test
    void withinBlocksOfAPoint() {
        runner.run("on load:\n    if player is within 5 blocks of 0, 64, 0:\n        send \"near\"\n    if player is not within 2 blocks of 100, 64, 100:\n        send \"far\"\n");
        assertEquals(List.of("near", "far"), runner.game.messages);
    }

    @Test
    void blockCommentsAreSkipped() {
        runner.run("###\non load:\n    send \"hidden\"\n###\non load:\n    send \"shown\"\n");
        assertEquals(List.of("shown"), runner.game.messages);
    }

    @Test
    void anUnclosedBlockCommentIsAnError() {
        assertEquals(List.of("t.ms:1: block comment \"###\" is never closed"), runner.errorsOf("###\non load:\n    send \"x\"\n"));
    }

    @Test
    void aTrailingBackslashJoinsTheNextLine() {
        runner.run("on load:\n    send \\\n        \"joined\"\n    send \"after\"\n");
        assertEquals(List.of("joined", "after"), runner.game.messages);
    }

    @Test
    void tryCatchesAndExposesTheError() {
        runner.run("on load:\n    try:\n        add \"x\" to {n}\n        send \"unreached\"\n    on error:\n        send \"caught %{_error}%\"\n    send \"after\"\n");
        assertEquals(List.of("caught cannot add text to a number", "after"), runner.game.messages);
        assertEquals(List.of(), runner.game.errors);
    }

    @Test
    void tryWithoutHandlerSwallows() {
        runner.run("on load:\n    try:\n        add \"x\" to {n}\n    send \"after\"\n");
        assertEquals(List.of("after"), runner.game.messages);
    }

    @Test
    void tryCatchesErrorsFromInsideFunctionsAndLoops() {
        runner.run("function bad():\n    add \"x\" to {n}\n\non load:\n    loop 3 times:\n        try:\n            bad()\n        on error:\n            send \"caught %loop-iteration%\"\n");
        assertEquals(List.of("caught 1", "caught 2", "caught 3"), runner.game.messages);
    }

    @Test
    void onErrorWithoutTryIsAParseError() {
        assertEquals(List.of("t.ms:2: \"on error\" without a matching \"try\""), runner.errorsOf("on load:\n    on error:\n        send \"x\"\n"));
    }
}
