package com.mineskript.scripttest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mineskript.doc.JSONGenerator;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.Pattern;
import com.mineskript.lang.parse.PatternElement;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScriptTestHarnessTest {
    private final ScriptTestHarness harness = new ScriptTestHarness();

    private List<String> run(String source) {
        ParsedScript script = harness.parse("t.ms", source);
        assertEquals(List.of(), script.errors().stream().map(ParseError::toString).toList());
        List<Trigger> tests = ScriptTestHarness.tests(script);
        assertEquals(1, tests.size());
        return harness.run(tests.get(0));
    }

    @Test
    void aFailingAssertReportsTheFileLineAndMessage() {
        assertEquals(List.of("t.ms:3: one is not two", "t.ms:4: assertion failed"),
                run("test \"x\":\n\tassert 1 is 1 with \"fine\"\n\tassert 1 is 2 with \"one is not two\"\n\tassert 1 is 3\n"));
    }

    @Test
    void theMessageCanUseValues() {
        assertEquals(List.of("t.ms:3: x was 5"), run("test:\n\tset {_x} to 5\n\tassert {_x} is 6 with \"x was %{_x}%\"\n"));
    }

    @Test
    void aPassingTestHasNoFailures() {
        assertEquals(List.of(), run("test:\n\tset {x} to 2\n\tassert {x} is 2 with \"set failed\"\n"));
    }

    @Test
    void anUncaughtScriptErrorFailsTheTest() {
        assertEquals(List.of("t.ms:2: division by zero"), run("test:\n\tset {_x} to 1 / 0\n\tassert true\n"));
    }

    @Test
    void runningOutOfStepsFailsTheTest() {
        List<String> failures = run("test:\n\tloop 20000 times:\n\t\tadd 1 to {_n}\n");
        assertEquals(1, failures.size());
        assertTrue(failures.get(0).endsWith("step limit exceeded"), failures.get(0));
    }

    @Test
    void waitsAdvanceSimulatedTicks() {
        assertEquals(List.of(), run("test:\n\tset {_t} to game time\n\twait 3 ticks\n"
                + "\tassert game time is {_t} + 3 with \"three ticks did not pass\"\n"));
    }

    @Test
    void waitUntilPassesOnceItsConditionDoes() {
        assertEquals(List.of(), run("test:\n\tset {_t} to game time\n\twait until game time is {_t} + 10\n"
                + "\tassert game time is {_t} + 10 with \"the wait ended at the wrong tick\"\n"));
    }

    @Test
    void waitUntilTimesOutLikeInGame() {
        assertEquals(List.of("t.ms:2: wait until timed out after 30 seconds"),
                run("test:\n\twait until {never} is set\n\tassert false with \"ran after the timeout\"\n"));
    }

    @Test
    void eachTestGetsFreshVariables() {
        ParsedScript script = harness.parse("t.ms", "test \"a\":\n\tset {x} to 1\n\ntest \"b\":\n"
                + "\tassert {x} is not set with \"x leaked\"\n");
        List<Trigger> tests = ScriptTestHarness.tests(script);
        assertEquals(List.of("a (line 1)", "b (line 4)"), tests.stream().map(ScriptTestHarness::label).toList());
        assertEquals(List.of(), harness.run(tests.get(0)));
        assertEquals(List.of(), harness.run(tests.get(1)));
    }

    @Test
    void aTestNameMustBePlainText() {
        ParsedScript script = harness.parse("t.ms", "test \"%{x}%\":\n\tassert true\n");
        assertEquals(List.of("t.ms:1: a test name must be plain text like \"adding\""),
                script.errors().stream().map(ParseError::toString).toList());
    }

    @Test
    void theShippedSyntaxHasNoTestEventOrAssert() {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms",
                "test:\n\tsend \"x\"\n\non load:\n\tassert 1 is 1 with \"x\"\n");
        assertEquals(List.of("t.ms:1: unknown event \"test\"", "t.ms:5: unknown effect \"assert 1 is 1 with \"x\"\""),
                script.errors().stream().map(ParseError::toString).toList());
    }

    @Test
    void theDocumentationHasNoTestSyntax() {
        String json = JSONGenerator.generate("test").toString();
        assertFalse(json.contains("EffAssert"));
        for (JsonElement event : JSONGenerator.generate("test").getAsJsonArray("events")) {
            assertFalse(event.getAsJsonObject().get("name").getAsString().contains("Test"));
        }
    }

    @Test
    void aConditionSlotIsMarkedInThePattern() {
        PatternElement.Sequence root = (PatternElement.Sequence) Pattern.compile("assert %condition%").root();
        PatternElement.Slot slot = (PatternElement.Slot) root.elements().get(1);
        assertTrue(slot.condition());
        assertFalse(((PatternElement.Slot) ((PatternElement.Sequence) Pattern.compile("x %number%").root())
                .elements().get(1)).condition());
    }
}
