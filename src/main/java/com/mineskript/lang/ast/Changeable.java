package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.Set;

public interface Changeable {
    String changeName();

    Set<ChangeMode> changeModes();

    default SkType changeType(ChangeMode mode) {
        return SkType.OBJECT;
    }

    default boolean changesWithMany() {
        return false;
    }

    void change(Context context, ChangeMode mode, Object value);
}
