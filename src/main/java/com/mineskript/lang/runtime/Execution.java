package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.LoopController;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import java.util.ArrayDeque;
import java.util.Deque;

public final class Execution {
    private static final class Frame {
        private final Block block;
        private final LoopController loop;
        private final int line;
        private int index;

        private Frame(Block block, LoopController loop, int line) {
            this.block = block;
            this.loop = loop;
            this.line = line;
        }
    }

    private final Trigger trigger;
    private final Context context;
    private final Deque<Frame> frames = new ArrayDeque<>();
    private int waitTicks;
    private Condition waitCondition;
    private int waitTimeoutTicks;
    private int waitLine;

    public Execution(Trigger trigger, Context context) {
        this.trigger = trigger;
        this.context = context;
        frames.push(new Frame(trigger.body(), null, trigger.line()));
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
            stop();
            throw error.at(context.file(), frame.line);
        } catch (RuntimeException error) {
            stop();
            String message = error.getMessage();
            throw new ScriptError(context.file(), frame.line, message == null ? error.getClass().getSimpleName() : message);
        }
    }

    private void popFrame() {
        Frame frame = frames.pop();
        if (frame.loop != null) {
            context.popLoop();
        }
    }

    void enter(Block block) {
        frames.push(new Frame(block, null, -1));
    }

    void enterLoop(Block block, LoopController loop, int line) {
        frames.push(new Frame(block, loop, line));
        context.pushLoop(loop.state());
    }

    void nextIteration() {
        while (!frames.isEmpty() && frames.peek().loop == null) {
            popFrame();
        }
        if (frames.isEmpty()) {
            throw new ScriptError("continue outside a loop");
        }
        Frame frame = frames.peek();
        frame.index = frame.block.statements().size();
    }

    void exitLoop() {
        while (!frames.isEmpty()) {
            Frame frame = frames.peek();
            popFrame();
            if (frame.loop != null) {
                return;
            }
        }
        throw new ScriptError("exit loop outside a loop");
    }

    void stop() {
        frames.clear();
        context.clearLoops();
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
