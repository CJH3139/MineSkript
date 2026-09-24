package com.mineskript.scripttest;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Execution;
import com.mineskript.lang.runtime.Functions;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.ScriptError;
import com.mineskript.lang.runtime.Variables;
import java.util.List;
import java.util.Map;

final class ScriptTestHarness {
    static final int STEP_BUDGET = 10_000;
    private static final int TICKS_PER_SECOND = 20;

    private final AssertionLog log = new AssertionLog();
    private final SyntaxRegistry registry = TestSyntax.registry(log);

    ParsedScript parse(String file, String source) {
        return new Parser(registry, new Functions()).parse(file, source);
    }

    static List<Trigger> tests(ParsedScript script) {
        return script.triggers().stream().filter(trigger -> TestSyntax.isTest(trigger.event())).toList();
    }

    static String label(Trigger trigger) {
        String name = TestSyntax.testName(trigger.event());
        return (name.isEmpty() ? "test" : name) + " (line " + trigger.line() + ")";
    }

    List<String> run(Trigger trigger) {
        return run(trigger, new FakeGameBridge());
    }

    List<String> run(Trigger trigger, FakeGameBridge game) {
        log.clear();
        Context context = new Context(game, trigger.file(), Map.of(), new Variables());
        Execution execution = new Execution(trigger, context);
        Interpreter interpreter = new Interpreter(STEP_BUDGET);
        try {
            while (interpreter.run(execution) == Interpreter.Outcome.WAITING) {
                if (!waitFor(execution, game)) {
                    log.add(new ScriptError(trigger.file(), execution.waitLine(),
                            "wait until timed out after " + WaitUntil.TIMEOUT_TICKS / TICKS_PER_SECOND + " seconds")
                            .toString());
                    break;
                }
            }
        } catch (ScriptError error) {
            log.add(error.toString());
        }
        game.errors.forEach(log::add);
        return log.failures();
    }

    private static boolean waitFor(Execution execution, FakeGameBridge game) {
        Condition condition = execution.waitCondition();
        if (condition == null) {
            advance(game, execution.waitTicks());
            return true;
        }
        for (int waited = 1; waited <= execution.waitTimeoutTicks(); waited++) {
            advance(game, 1);
            if (condition.test(execution.context())) {
                return true;
            }
        }
        return false;
    }

    private static void advance(FakeGameBridge game, int ticks) {
        game.gameTime += ticks;
        game.dayTime += ticks;
    }
}
