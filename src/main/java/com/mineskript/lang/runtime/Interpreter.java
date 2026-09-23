package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;

public final class Interpreter {
    public enum Outcome {
        DONE,
        WAITING
    }

    private final int stepBudget;

    public Interpreter(int stepBudget) {
        this.stepBudget = stepBudget;
    }

    public int stepBudget() {
        return stepBudget;
    }

    public Outcome run(Execution execution) {
        int steps = 0;
        Statement statement;
        while ((statement = execution.next()) != null) {
            if (++steps > stepBudget) {
                String file = execution.context().file();
                execution.stop();
                throw new ScriptError(file, statement.line(), "step limit exceeded");
            }
            try {
                Flow flow = statement.execute(execution.context());
                switch (flow) {
                    case Flow.Continue ignored -> {
                    }
                    case Flow.Enter enter -> execution.enter(enter.block());
                    case Flow.EnterLoop loop -> execution.enterLoop(loop.block(), loop.controller(), statement.line());
                    case Flow.NextIteration ignored -> execution.nextIteration();
                    case Flow.ExitLoop ignored -> execution.exitLoop();
                    case Flow.Stop ignored -> execution.stop();
                    case Flow.Call call -> execution.call(call.function(), call.arguments(), statement.line());
                    case Flow.Return result -> execution.returnFrom(result.value());
                    case Flow.Wait wait -> {
                        execution.suspend(wait.ticks());
                        return Outcome.WAITING;
                    }
                    case Flow.Park park -> {
                        execution.park(park.condition(), park.timeoutTicks(), statement.line());
                        return Outcome.WAITING;
                    }
                }
            } catch (ScriptError error) {
                String file = execution.context().file();
                execution.stop();
                throw error.at(file, statement.line());
            } catch (RuntimeException error) {
                String file = execution.context().file();
                execution.stop();
                String message = error.getMessage();
                throw new ScriptError(file, statement.line(), message == null ? error.getClass().getSimpleName() : message);
            }
        }
        return Outcome.DONE;
    }
}
