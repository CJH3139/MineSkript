package com.mineskript.lang.runtime;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Function;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import java.util.ArrayList;
import java.util.List;

public final class FunctionCall implements Expression, Statement {
    public static final int STEP_BUDGET = 10_000;

    private final Function target;
    private final String callerFile;
    private final Functions functions;
    private final List<Expression> arguments;
    private final int line;

    public FunctionCall(Function target, String callerFile, Functions functions, List<Expression> arguments, int line) {
        this.target = target;
        this.callerFile = callerFile;
        this.functions = functions;
        this.arguments = List.copyOf(arguments);
        this.line = line;
    }

    @Override
    public SkType type() {
        return target.returnType() == null ? SkType.OBJECT : target.returnType();
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Object evaluate(Context context) {
        Function function = resolve();
        if (!function.returns()) {
            throw new ScriptError("function \"" + function.name() + "\" doesn't return anything");
        }
        Execution call = Execution.ofCall(function, values(function, context), context, line);
        if (new Interpreter(STEP_BUDGET).run(call) == Interpreter.Outcome.WAITING) {
            call.stop();
            throw new ScriptError("function \"" + function.name() + "\" can't wait when its value is used, call it on its own line instead");
        }
        Object result = call.returned();
        return result == None.NONE ? None.NONE : Converters.convert(result, function.returnType(), context);
    }

    @Override
    public Flow execute(Context context) {
        Function function = resolve();
        return new Flow.Call(function, values(function, context));
    }

    private Function resolve() {
        if (target.file().equals(callerFile)) {
            return target;
        }
        return functions.loaded(target.name(), callerFile).orElse(target);
    }

    private List<Object> values(Function function, Context context) {
        List<Function.Parameter> parameters = function.parameters();
        if (arguments.size() > parameters.size() || arguments.size() < function.requiredParameters()) {
            throw new ScriptError("function \"" + function.name() + "\" no longer takes " + arguments.size() + " arguments");
        }
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            Function.Parameter parameter = parameters.get(i);
            Object value = i < arguments.size() ? arguments.get(i).evaluate(context) : parameter.fallback().evaluate(context);
            values.add(value == None.NONE ? None.NONE : Converters.convert(value, parameter.type(), context));
        }
        return values;
    }
}
