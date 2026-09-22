package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import java.util.ArrayDeque;
import java.util.Deque;

public final class Execution {
    private static final class Frame {
        private final Block block;
        private int index;

        private Frame(Block block) {
            this.block = block;
        }
    }

    private final Trigger trigger;
    private final Context context;
    private final Deque<Frame> frames = new ArrayDeque<>();
    private int waitTicks;

    public Execution(Trigger trigger, Context context) {
        this.trigger = trigger;
        this.context = context;
        frames.push(new Frame(trigger.body()));
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

    public boolean finished() {
        return frames.isEmpty();
    }

    Statement next() {
        while (!frames.isEmpty()) {
            Frame frame = frames.peek();
            if (frame.index < frame.block.statements().size()) {
                return frame.block.statements().get(frame.index++);
            }
            frames.pop();
        }
        return null;
    }

    void enter(Block block) {
        frames.push(new Frame(block));
    }

    void stop() {
        frames.clear();
    }

    void suspend(int ticks) {
        waitTicks = ticks;
    }
}
