package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public interface Expression {
    SkType type();

    Object evaluate(Context context);

    default boolean isList() {
        return false;
    }

    default Optional<Changeable> changer() {
        return this instanceof Changeable changeable ? Optional.of(changeable) : Optional.empty();
    }
}
