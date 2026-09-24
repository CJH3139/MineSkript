package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.List;

public record LoopStatement(int line, Kind kind, Block body) implements Statement {
    public sealed interface Kind {
    }

    public record Times(Expression count) implements Kind {
    }

    public record Over(Expression list) implements Kind {
    }

    public record While(Condition condition) implements Kind {
    }

    @Override
    public Flow execute(Context context) {
        LoopController controller = switch (kind) {
            case Times times -> new TimesLoop(count(times.count().evaluate(context)));
            case Over over -> new OverLoop(items(over.list().evaluate(context)));
            case While loop -> new WhileLoop(loop.condition());
        };
        return controller.advance(context) ? new Flow.EnterLoop(body, controller) : Flow.CONTINUE;
    }

    private static int count(Object value) {
        return (int) Math.max(0, Math.round((Double) value));
    }

    private static List<?> items(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        if (value == None.NONE) {
            return List.of();
        }
        return List.of(value);
    }

    private static final class TimesLoop implements LoopController {
        private final LoopState state = new LoopState();
        private final int total;
        private int current;

        private TimesLoop(int total) {
            this.total = total;
        }

        @Override
        public boolean advance(Context context) {
            if (current >= total) {
                return false;
            }
            current++;
            state.next((double) current);
            return true;
        }

        @Override
        public LoopState state() {
            return state;
        }
    }

    private static final class OverLoop implements LoopController {
        private final LoopState state = new LoopState();
        private final List<?> items;
        private int index;

        private OverLoop(List<?> items) {
            this.items = items;
        }

        @Override
        public boolean advance(Context context) {
            if (index >= items.size()) {
                return false;
            }
            String key = items instanceof IndexedValues indexed ? indexed.index(index) : null;
            state.next(items.get(index++), key);
            return true;
        }

        @Override
        public LoopState state() {
            return state;
        }
    }

    private static final class WhileLoop implements LoopController {
        private final LoopState state = new LoopState();
        private final Condition condition;

        private WhileLoop(Condition condition) {
            this.condition = condition;
        }

        @Override
        public boolean advance(Context context) {
            if (!condition.test(context)) {
                return false;
            }
            state.next(None.NONE);
            return true;
        }

        @Override
        public LoopState state() {
            return state;
        }
    }
}
