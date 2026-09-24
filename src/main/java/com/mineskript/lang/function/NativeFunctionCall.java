package com.mineskript.lang.function;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.ArrayList;
import java.util.List;

public final class NativeFunctionCall implements Expression, Statement {
    private final FunctionInfo function;
    private final List<Expression> arguments;
    private final int line;

    public NativeFunctionCall(FunctionInfo function, List<Expression> arguments, int line) {
        this.function = function;
        this.arguments = List.copyOf(arguments);
        this.line = line;
    }

    public FunctionInfo function() {
        return function;
    }

    @Override
    public SkType type() {
        return function.returnType();
    }

    @Override
    public boolean isList() {
        if (!function.listResult()) {
            return false;
        }
        for (int i = 0; i < arguments.size(); i++) {
            if (function.parameters().get(i).list() && arguments.get(i).isList()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Object evaluate(Context context) {
        List<Object> values = new ArrayList<>();
        List<FunctionParameter> parameters = function.parameters();
        for (int i = 0; i < parameters.size(); i++) {
            FunctionParameter parameter = parameters.get(i);
            Object value = i < arguments.size()
                    ? arguments.get(i).evaluate(context)
                    : parameter.defaultValue(context);
            Object converted = parameter.list() ? items(value, parameter.type(), context) : single(value,
                    parameter.type(), context);
            if (converted == None.NONE) {
                return None.NONE;
            }
            values.add(converted);
        }
        Object result = function.body().call(values, context);
        if (result instanceof List<?> list && !isList()) {
            return list.isEmpty() ? None.NONE : list.getFirst();
        }
        return result == null ? None.NONE : result;
    }

    @Override
    public Flow execute(Context context) {
        evaluate(context);
        return Flow.CONTINUE;
    }

    private static Object single(Object value, SkType type, Context context) {
        if (value == None.NONE || value instanceof List<?> list && list.isEmpty()) {
            return None.NONE;
        }
        return Converters.convert(value instanceof List<?> list ? list.getFirst() : value, type, context);
    }

    private static Object items(Object value, SkType type, Context context) {
        List<Object> items = new ArrayList<>();
        flatten(value, type, context, items);
        return items.isEmpty() ? None.NONE : List.copyOf(items);
    }

    private static void flatten(Object value, SkType type, Context context, List<Object> into) {
        if (value instanceof List<?> list) {
            for (Object item : list) {
                flatten(item, type, context, into);
            }
        } else if (value != None.NONE && value != null) {
            into.add(Converters.convert(value, type, context));
        }
    }
}
