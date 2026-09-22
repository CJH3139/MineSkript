package com.mineskript.lang.ast;

public sealed interface Flow {
    Flow CONTINUE = new Continue();
    Flow STOP = new Stop();

    record Continue() implements Flow {
    }

    record Stop() implements Flow {
    }

    record Enter(Block block) implements Flow {
    }

    record Wait(int ticks) implements Flow {
    }
}
