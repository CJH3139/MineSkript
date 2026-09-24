package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.Set;

/**
 * An expression that the set, add, remove, delete and reset effects can change, like Skript's changers. The effects
 * check {@link #changeModes()} and {@link #changeType(ChangeMode)} while parsing, so {@link #change} only ever sees a
 * supported mode and a value already converted to the requested type.
 */
public interface Changeable {
    /** How the expression is named in parse errors, such as "the yaw". */
    String changeName();

    /** The modes this expression supports where it was parsed; empty when it cannot be changed there at all. */
    Set<ChangeMode> changeModes();

    /** The type a value must have for the given mode; {@link SkType#OBJECT} takes anything. */
    default SkType changeType(ChangeMode mode) {
        return SkType.OBJECT;
    }

    /** Whether the value may be a list of several values rather than one. */
    default boolean changesWithMany() {
        return false;
    }

    /** Applies the change. The value is {@link None#NONE} for delete and reset. */
    void change(Context context, ChangeMode mode, Object value);
}
