package com.mineskript.lang.function;

import com.mineskript.lang.runtime.Context;
import java.util.List;

/**
 * What a built-in function does when it is called, written in Java.
 *
 * <p>It receives one value per parameter, in order: a single value already converted to the parameter's type (a
 * {@code Double} for a number, a {@code String} for text, and so on), or, for a list parameter, a non-empty
 * {@code List} of such values. Parameters left out by the script arrive as their default. It is never called with a
 * missing value: when any argument has no value, or a list argument is empty, the call gives none without running
 * the body, like Skript's simple Java functions.
 */
@FunctionalInterface
public interface FunctionBody {
    /**
     * Runs the function and returns its result: a value of the function's return type, a {@code List} of them for a
     * function registered with {@link FunctionInfo#returnsList()}, or {@code None.NONE} (or {@code null}) for no
     * value. Throw {@code ScriptError} to stop the script line with a message for the script writer.
     */
    Object call(List<Object> arguments, Context context);
}
