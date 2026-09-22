package com.mineskript.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.IfChain;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InterpreterTest {
    private final FakeGameBridge game = new FakeGameBridge();

    private static Statement say(String text) {
        return new Statement() {
            @Override
            public int line() {
                return 1;
            }

            @Override
            public Flow execute(Context context) {
                context.game().showMessage(text);
                return Flow.CONTINUE;
            }
        };
    }

    private static Statement flow(int line, Flow flow) {
        return new Statement() {
            @Override
            public int line() {
                return line;
            }

            @Override
            public Flow execute(Context context) {
                return flow;
            }
        };
    }

    private static Statement failing(int line, RuntimeException error) {
        return new Statement() {
            @Override
            public int line() {
                return line;
            }

            @Override
            public Flow execute(Context context) {
                throw error;
            }
        };
    }

    private static Condition when(boolean value) {
        return context -> value;
    }

    private Execution execution(Statement... statements) {
        Trigger trigger = new Trigger("t.ms", 1, new Event.Load(), new Block(List.of(statements)));
        return new Execution(trigger, new Context(game, "t.ms", Map.of()));
    }

    @Test
    void runsStatementsInOrder() {
        Interpreter interpreter = new Interpreter(100);
        Execution execution = execution(say("a"), say("b"));
        assertEquals(Interpreter.Outcome.DONE, interpreter.run(execution));
        assertEquals(List.of("a", "b"), game.messages);
        assertTrue(execution.finished());
    }

    @Test
    void entersTheFirstTrueBranchAndContinuesAfterIt() {
        IfChain chain = new IfChain(2, List.of(
                new IfChain.Branch(when(false), new Block(List.of(say("no")))),
                new IfChain.Branch(when(true), new Block(List.of(say("yes"))))),
                new Block(List.of(say("else"))));
        new Interpreter(100).run(execution(chain, say("after")));
        assertEquals(List.of("yes", "after"), game.messages);
    }

    @Test
    void fallsThroughToElse() {
        IfChain chain = new IfChain(2, List.of(new IfChain.Branch(when(false), new Block(List.of(say("no"))))), new Block(List.of(say("else"))));
        new Interpreter(100).run(execution(chain));
        assertEquals(List.of("else"), game.messages);
    }

    @Test
    void ifWithoutElseAndNoTrueBranchDoesNothing() {
        IfChain chain = new IfChain(2, List.of(new IfChain.Branch(when(false), new Block(List.of(say("no"))))), null);
        new Interpreter(100).run(execution(chain, say("after")));
        assertEquals(List.of("after"), game.messages);
    }

    @Test
    void stopEndsTheExecutionEvenInsideNestedBlocks() {
        IfChain chain = new IfChain(2, List.of(new IfChain.Branch(when(true), new Block(List.of(say("in"), flow(3, Flow.STOP), say("never"))))), null);
        Execution execution = execution(chain, say("never either"));
        assertEquals(Interpreter.Outcome.DONE, new Interpreter(100).run(execution));
        assertEquals(List.of("in"), game.messages);
        assertTrue(execution.finished());
    }

    @Test
    void waitSuspendsAndResumesAtTheNextStatement() {
        Interpreter interpreter = new Interpreter(100);
        Execution execution = execution(say("before"), flow(2, new Flow.Wait(20)), say("after"));
        assertEquals(Interpreter.Outcome.WAITING, interpreter.run(execution));
        assertEquals(20, execution.waitTicks());
        assertEquals(List.of("before"), game.messages);
        assertEquals(Interpreter.Outcome.DONE, interpreter.run(execution));
        assertEquals(List.of("before", "after"), game.messages);
    }

    @Test
    void waitInsideNestedBlockResumesInsideThatBlock() {
        IfChain chain = new IfChain(2, List.of(new IfChain.Branch(when(true), new Block(List.of(say("a"), flow(3, new Flow.Wait(1)), say("b"))))), null);
        Interpreter interpreter = new Interpreter(100);
        Execution execution = execution(chain, say("c"));
        interpreter.run(execution);
        interpreter.run(execution);
        assertEquals(List.of("a", "b", "c"), game.messages);
    }

    @Test
    void stepBudgetAbortsWithLocatedError() {
        Statement[] many = new Statement[12];
        for (int i = 0; i < many.length; i++) {
            many[i] = say("s" + i);
        }
        ScriptError error = assertThrows(ScriptError.class, () -> new Interpreter(10).run(execution(many)));
        assertEquals("t.ms:1: step limit exceeded", error.toString());
    }

    @Test
    void budgetResetsAfterAWait() {
        Statement[] many = new Statement[9];
        for (int i = 0; i < many.length; i++) {
            many[i] = say("s" + i);
        }
        Interpreter interpreter = new Interpreter(10);
        Execution execution = execution(say("x"), say("y"), say("z"), flow(2, new Flow.Wait(1)), many[0], many[1], many[2], many[3], many[4], many[5], many[6], many[7], many[8]);
        assertEquals(Interpreter.Outcome.WAITING, interpreter.run(execution));
        assertEquals(Interpreter.Outcome.DONE, interpreter.run(execution));
    }

    @Test
    void unlocatedScriptErrorsGainTheStatementLine() {
        ScriptError error = assertThrows(ScriptError.class, () -> new Interpreter(10).run(execution(failing(7, new ScriptError("no world")))));
        assertEquals("t.ms:7: no world", error.toString());
    }

    @Test
    void otherRuntimeExceptionsBecomeLocatedScriptErrors() {
        ScriptError error = assertThrows(ScriptError.class, () -> new Interpreter(10).run(execution(failing(4, new IllegalStateException("boom")))));
        assertEquals("t.ms:4: boom", error.toString());
    }
}
