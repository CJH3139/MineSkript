package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public interface Expression {
    SkType type();

    Object evaluate(Context context);

    default boolean isList() {
        return false;
    }

    /** What the set, add, remove, delete and reset effects can do to this expression; empty when nothing. */
    default Optional<Changeable> changer() {
        return this instanceof Changeable changeable ? Optional.of(changeable) : Optional.empty();
    }
}
