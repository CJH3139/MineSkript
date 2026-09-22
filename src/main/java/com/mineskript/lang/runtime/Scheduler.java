package com.mineskript.lang.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Scheduler {
    private record Entry(Execution execution, long resumeTick) {
    }

    private final List<Entry> waiting = new ArrayList<>();

    public void schedule(Execution execution, long resumeTick) {
        waiting.add(new Entry(execution, resumeTick));
    }

    public List<Execution> drain(long now) {
        List<Execution> due = new ArrayList<>();
        Iterator<Entry> iterator = waiting.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.resumeTick() <= now) {
                due.add(entry.execution());
                iterator.remove();
            }
        }
        return due;
    }

    public void clear() {
        waiting.clear();
    }

    public int size() {
        return waiting.size();
    }
}
