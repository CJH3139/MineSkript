package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;

public interface Statement {
    int line();

    Flow execute(Context context);
}
