package com.mineskript.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Trigger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SchedulerTest {
    private Execution execution(String file) {
        Trigger trigger = new Trigger(file, 1, new Event.Load(), new Block(List.of()));
        return new Execution(trigger, new Context(new FakeGameBridge(), file, Map.of()));
    }

    @Test
    void drainsOnlyDueExecutionsInInsertionOrder() {
        Scheduler scheduler = new Scheduler();
        Execution a = execution("a");
        Execution b = execution("b");
        Execution c = execution("c");
        scheduler.schedule(a, 10);
        scheduler.schedule(b, 5);
        scheduler.schedule(c, 10);
        assertTrue(scheduler.drain(4).isEmpty());
        assertEquals(List.of(b), scheduler.drain(5));
        assertEquals(List.of(a, c), scheduler.drain(10));
        assertEquals(0, scheduler.size());
    }

    @Test
    void clearDropsEverything() {
        Scheduler scheduler = new Scheduler();
        scheduler.schedule(execution("a"), 1);
        scheduler.clear();
        assertEquals(0, scheduler.size());
        assertTrue(scheduler.drain(100).isEmpty());
    }
}
