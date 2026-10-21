package com.mineskript.lang.parse;

/** Loads a top-level structure an addon registered. Throwing {@link SyntaxException} makes it a parse error. */
@FunctionalInterface
public interface StructureHandler {
    void load(Structure structure);
}
