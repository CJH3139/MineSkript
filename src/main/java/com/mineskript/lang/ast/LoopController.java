package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;

public interface LoopController {
    boolean advance(Context context);

    LoopState state();
}
