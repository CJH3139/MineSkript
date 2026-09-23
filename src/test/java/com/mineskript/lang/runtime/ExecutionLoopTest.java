package com.mineskript.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.LoopStatement;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ConstantExpression;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExecutionLoopTest {
    private final FakeGameBridge game = new FakeGameBridge();

    private static Statement say(int line, String text) {
        return new Statement() {
            @Override
            public int line() {
                return line;
            }

            @Override
            public Flow execute(Context context) {
                context.game().showMessage(text + context.currentLoop().iteration());
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

    private Execution execution(Statement... statements) {
        Trigger trigger = new Trigger("t.ms", 1, new Event.Load(), new Block(List.of(statements)));
        return new Execution(trigger, new Context(game, "t.ms", Map.of()));
    }

    @Test
    void timesLoopRunsItsBodyThatManyTimes() {
        LoopStatement loop = new LoopStatement(2, new LoopStatement.Times(new ConstantExpression(SkType.NUMBER, 2.0)), new Block(List.of(say(3, "a"))));
        new Interpreter(100).run(execution(loop, flow(4, Flow.CONTINUE)));
        assertEquals(List.of("a1", "a2"), game.messages);
    }

    @Test
    void continueOutsideALoopIsALocatedError() {
        ScriptError error = assertThrows(ScriptError.class, () -> new Interpreter(100).run(execution(flow(3, Flow.NEXT_ITERATION))));
        assertEquals("t.ms:3: continue outside a loop", error.toString());
    }

    @Test
    void exitLoopOutsideALoopIsALocatedError() {
        ScriptError error = assertThrows(ScriptError.class, () -> new Interpreter(100).run(execution(flow(5, Flow.EXIT_LOOP))));
        assertEquals("t.ms:5: exit loop outside a loop", error.toString());
    }
}
