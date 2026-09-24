package com.mineskript.lang.function;

import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;
import java.util.function.Function;

/**
 * One parameter of a built-in function: its name and type, whether it takes a list of values, and the default used
 * when a script leaves it out. Parameters with a default must come after every parameter without one.
 */
public final class FunctionParameter {
    private final String name;
    private final SkType type;
    private final boolean list;
    private final Function<Context, Object> fallback;
    private final String fallbackText;

    private FunctionParameter(String name, SkType type, boolean list, Function<Context, Object> fallback,
            String fallbackText) {
        this.name = name;
        this.type = type;
        this.list = list;
        this.fallback = fallback;
        this.fallbackText = fallbackText;
    }

    /** A parameter that takes one value and must always be given. */
    public static FunctionParameter of(String name, SkType type) {
        return new FunctionParameter(name, type, false, null, null);
    }

    /**
     * A parameter that takes any number of values, such as the numbers of {@code max(1, 2, 3)}. When it is a
     * function's only parameter, every argument of a call goes into it, so {@code max(1, {_list::*}, 3)} works.
     * Otherwise a script gives it one argument, which may be a list such as {@code {_list::*}} or {@code (1, 2, 3)}.
     */
    public static FunctionParameter list(String name, SkType type) {
        return new FunctionParameter(name, type, true, null, null);
    }

    /** A parameter that takes one value and is {@code value} when a script leaves it out. */
    public static FunctionParameter optional(String name, SkType type, Object value) {
        String shown = value instanceof String text ? "\"" + text + "\"" : Converters.toText(value, null);
        return new FunctionParameter(name, type, false, context -> value, shown);
    }

    /**
     * A parameter that takes one value and, when a script leaves it out, is worked out when the function is called,
     * such as the dimension you are in. {@code shown} describes the default in the documentation.
     */
    public static FunctionParameter optional(String name, SkType type, String shown,
            Function<Context, Object> fallback) {
        return new FunctionParameter(name, type, false, fallback, shown);
    }

    /** The name, shown in the documentation and in error messages. */
    public String name() {
        return name;
    }

    /** The type each value is converted to before the function body sees it. */
    public SkType type() {
        return type;
    }

    /** Whether this parameter takes a list of values. */
    public boolean list() {
        return list;
    }

    /** Whether a script may leave this parameter out. */
    public boolean optional() {
        return fallback != null;
    }

    /** How the documentation shows the default, if there is one. */
    public Optional<String> defaultText() {
        return Optional.ofNullable(fallbackText);
    }

    /** The value used when a script leaves this parameter out. */
    public Object defaultValue(Context context) {
        if (fallback == null) {
            throw new IllegalStateException("parameter " + name + " has no default");
        }
        return fallback.apply(context);
    }

    /** How the parameter reads in a signature, such as {@code d: number = 0} or {@code ns: numbers}. */
    public String signature() {
        String typeName = Converters.typeName(type);
        if (list) {
            typeName = typeName.endsWith("y") ? typeName.substring(0, typeName.length() - 1) + "ies" : typeName + "s";
        }
        return name + ": " + typeName + (fallbackText == null ? "" : " = " + fallbackText);
    }
}
