package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;

public interface Condition {
    boolean test(Context context);
}
