package com.mineskript.lang.function;

import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Converters;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * One built-in function, like Skript's Java functions such as {@code round} or {@code location}: its name,
 * parameters, return type and the Java body that runs, with its documentation filled in by the chained calls after
 * {@link com.mineskript.lang.parse.SyntaxRegistry#addFunction}, the same way events are documented.
 *
 * <p>Scripts call it as {@code name(arguments)}, anywhere a value can go or on a line of its own. Function names
 * ignore case, so {@code isNaN} is also {@code isnan}. A script may not define a function with the same name.
 */
public final class FunctionInfo {
    private static final java.util.regex.Pattern NAME = java.util.regex.Pattern.compile("[a-z_][a-z0-9_]*");

    private final String name;
    private final SkType returnType;
    private final FunctionBody body;
    private final List<FunctionParameter> parameters;
    private boolean returnsList;
    private String[] description = new String[0];
    private String[] examples = new String[0];
    private String[] since = new String[0];
    private String[] keywords = new String[0];

    /**
     * Makes a function. Addons register functions with
     * {@link com.mineskript.lang.parse.SyntaxRegistry#addFunction} rather than calling this.
     *
     * @throws IllegalArgumentException if the name is not a valid function name or a parameter without a default
     *     follows one with a default
     */
    public FunctionInfo(String name, SkType returnType, FunctionBody body, List<FunctionParameter> parameters) {
        if (name == null || !NAME.matcher(name.toLowerCase(Locale.ROOT)).matches()) {
            throw new IllegalArgumentException("\"" + name + "\" is not a valid function name");
        }
        boolean defaulted = false;
        for (FunctionParameter parameter : parameters) {
            if (defaulted && !parameter.optional()) {
                throw new IllegalArgumentException("function " + name + ": parameter " + parameter.name()
                        + " needs a default because an earlier parameter has one");
            }
            defaulted |= parameter.optional();
        }
        this.name = name;
        this.returnType = returnType;
        this.body = body;
        this.parameters = List.copyOf(parameters);
    }

    /**
     * Declares that the function gives one value for each value of its list parameter, like Skript's
     * {@code clamp}: a call is a list when that argument is a list, and a single value otherwise.
     */
    public FunctionInfo returnsList() {
        returnsList = true;
        return this;
    }

    /** What the function does, one string per paragraph. */
    public FunctionInfo description(String... description) {
        this.description = description.clone();
        return this;
    }

    /** Complete example scripts, one string per line, with {@code ""} between separate scripts. */
    public FunctionInfo examples(String... examples) {
        this.examples = examples.clone();
        return this;
    }

    /** The version the function first shipped in. */
    public FunctionInfo since(String... since) {
        this.since = since.clone();
        return this;
    }

    /** Extra words the documentation search should find the function by. */
    public FunctionInfo keywords(String... keywords) {
        this.keywords = keywords.clone();
        return this;
    }

    /** The name as registered, such as {@code isNaN}. */
    public String name() {
        return name;
    }

    /** The name scripts are matched against: the registered name in lower case. */
    public String key() {
        return name.toLowerCase(Locale.ROOT);
    }

    public SkType returnType() {
        return returnType;
    }

    public FunctionBody body() {
        return body;
    }

    public List<FunctionParameter> parameters() {
        return parameters;
    }

    public boolean listResult() {
        return returnsList;
    }

    /** How many arguments a call must give at least: the parameters without a default. */
    public int requiredParameters() {
        return (int) parameters.stream().filter(parameter -> !parameter.optional()).count();
    }

    /** The function as documented, such as {@code round(n: number, d: number = 0) :: number}. */
    public String signature() {
        StringJoiner joined = new StringJoiner(", ", name + "(", ")");
        parameters.forEach(parameter -> joined.add(parameter.signature()));
        String result = Converters.typeName(returnType) + (returnsList ? "s" : "");
        return joined + " :: " + result;
    }

    public List<String> description() {
        return List.of(description);
    }

    public List<String> examples() {
        return List.of(examples);
    }

    public List<String> since() {
        return List.of(since);
    }

    public List<String> keywords() {
        return List.of(keywords);
    }
}
