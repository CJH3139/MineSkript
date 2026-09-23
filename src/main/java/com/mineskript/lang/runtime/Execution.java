package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Function;
import com.mineskript.lang.ast.LoopController;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Execution {
    public static final int MAX_CALL_DEPTH = 100;

    private static final class Frame {
        private final Block block;
        private final LoopController loop;
        private final int line;
        private final boolean function;
        private int index;

        private Frame(Block block, LoopController loop, int line, boolean function) {
            this.block = block;
            this.loop = loop;
            this.line = line;
            this.function = function;
        }
    }

    private final Trigger trigger;
    private final Context context;
    private final Deque<Frame> frames = new ArrayDeque<>();
    private int waitTicks;
    private Condition waitCondition;
    private int waitTimeoutTicks;
    private int waitLine;
    private Object returned = None.NONE;

    public Execution(Trigger trigger, Context context) {
        this.trigger = trigger;
        this.context = context;
        frames.push(new Frame(trigger.body(), null, trigger.line(), false));
    }

    private Execution(Context context) {
        this.trigger = null;
        this.context = context;
    }

    static Execution ofCall(Function function, List<Object> arguments, Context context, int line) {
        Execution execution = new Execution(context);
        execution.call(function, arguments, line);
        return execution;
    }

    public Trigger trigger() {
        return trigger;
    }

    public Context context() {
        return context;
    }

    public int waitTicks() {
        return waitTicks;
    }

    public Condition waitCondition() {
        return waitCondition;
    }

    public int waitTimeoutTicks() {
        return waitTimeoutTicks;
    }

    public int waitLine() {
        return waitLine;
    }

    public boolean finished() {
        return frames.isEmpty();
    }

    Object returned() {
        return returned;
    }

    Statement next() {
        while (!frames.isEmpty()) {
            Frame frame = frames.peek();
            if (frame.index < frame.block.statements().size()) {
                return frame.block.statements().get(frame.index++);
            }
            if (frame.loop != null) {
                context.popLoop();
                boolean more = advance(frame);
                context.pushLoop(frame.loop.state());
                if (more) {
                    frame.index = 0;
                    continue;
                }
            }
            popFrame();
        }
        return null;
    }

    private boolean advance(Frame frame) {
        try {
            return frame.loop.advance(context);
        } catch (ScriptError error) {
            String file = context.file();
            context.pushLoop(frame.loop.state());
            stop();
            throw error.at(file, frame.line);
        } catch (RuntimeException error) {
            String file = context.file();
            context.pushLoop(frame.loop.state());
            stop();
            String message = error.getMessage();
            throw new ScriptError(file, frame.line, message == null ? error.getClass().getSimpleName() : message);
        }
    }

    private void popFrame() {
        Frame frame = frames.pop();
        if (frame.loop != null) {
            context.popLoop();
        }
        if (frame.function) {
            context.exitFunction();
        }
    }

    void enter(Block block) {
        frames.push(new Frame(block, null, -1, false));
    }

    void enterLoop(Block block, LoopController loop, int line) {
        frames.push(new Frame(block, loop, line, false));
        context.pushLoop(loop.state());
    }

    void call(Function function, List<Object> arguments, int line) {
        if (context.functionDepth() >= MAX_CALL_DEPTH) {
            throw new ScriptError("function \"" + function.name() + "\" went more than " + MAX_CALL_DEPTH + " calls deep");
        }
        if (function.body() == null) {
            throw new ScriptError("function \"" + function.name() + "\" failed to load");
        }
        Map<String, Object> locals = new HashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            locals.put(function.parameters().get(i).name(), arguments.get(i));
        }
        frames.push(new Frame(function.body(), null, line, true));
        context.enterFunction(function.file(), locals);
    }

    void returnFrom(Object value) {
        while (!frames.isEmpty()) {
            Frame frame = frames.peek();
            popFrame();
            if (frame.function) {
                returned = value;
                return;
            }
        }
        throw new ScriptError("return outside a function");
    }

    void nextIteration() {
        while (!frames.isEmpty() && frames.peek().loop == null && !frames.peek().function) {
            popFrame();
        }
        if (frames.isEmpty() || frames.peek().function) {
            throw new ScriptError("continue outside a loop");
        }
        Frame frame = frames.peek();
        frame.index = frame.block.statements().size();
    }

    void exitLoop() {
        while (!frames.isEmpty() && !frames.peek().function) {
            Frame frame = frames.peek();
            popFrame();
            if (frame.loop != null) {
                return;
            }
        }
        throw new ScriptError("exit loop outside a loop");
    }

    void stop() {
        while (!frames.isEmpty()) {
            popFrame();
        }
    }

    void suspend(int ticks) {
        waitTicks = ticks;
        waitCondition = null;
    }

    void park(Condition condition, int timeoutTicks, int line) {
        waitTicks = 1;
        waitCondition = condition;
        waitTimeoutTicks = timeoutTicks;
        waitLine = line;
    }
}
