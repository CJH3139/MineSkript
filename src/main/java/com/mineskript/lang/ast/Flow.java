package com.mineskript.lang.ast;

import java.util.List;

public sealed interface Flow {
    Flow CONTINUE = new Continue();
    Flow STOP = new Stop();
    Flow NEXT_ITERATION = new NextIteration();
    Flow EXIT_LOOP = new ExitLoop();

    record Continue() implements Flow {
    }

    record Stop() implements Flow {
    }

    record Enter(Block block) implements Flow {
    }

    record Wait(int ticks) implements Flow {
    }

    record Park(Condition condition, int timeoutTicks) implements Flow {
    }

    record EnterLoop(Block block, LoopController controller) implements Flow {
    }

    record EnterTry(Block block, Block handler) implements Flow {
    }

    record NextIteration() implements Flow {
    }

    record ExitLoop() implements Flow {
    }

    record Call(Function function, List<Object> arguments) implements Flow {
    }

    record Return(Object value) implements Flow {
    }
}
