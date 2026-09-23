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
        while (true) {
            Statement statement;
            try {
                statement = execution.next();
            } catch (ScriptError error) {
                if (execution.recover(error)) {
                    continue;
                }
                execution.stop();
                throw error;
            }
            if (statement == null) {
                return Outcome.DONE;
            }
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
                    case Flow.EnterTry attempt -> execution.enterTry(attempt.block(), attempt.handler());
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
                ScriptError located = error.at(execution.context().file(), statement.line());
                if (!execution.recover(located)) {
                    execution.stop();
                    throw located;
                }
            } catch (RuntimeException error) {
                String message = error.getMessage();
                ScriptError located = new ScriptError(execution.context().file(), statement.line(),
                        message == null ? error.getClass().getSimpleName() : message);
                if (!execution.recover(located)) {
                    execution.stop();
                    throw located;
                }
            }
        }
    }
}
