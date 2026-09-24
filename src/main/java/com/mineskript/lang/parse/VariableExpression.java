package com.mineskript.lang.parse;

import com.mineskript.lang.Language;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.IndexedValues;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.lexer.TextScanner;
import com.mineskript.lang.runtime.Comparators;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.Relation;
import com.mineskript.lang.runtime.ScriptError;
import com.mineskript.lang.runtime.VariableScope;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A variable such as {count}, {-session}, {_temp}, one list entry such as {homes::alex} or a whole list such as
 * {homes::*}. Parts of the name written as %expression% are evaluated each time, so {homes::%player%} names a
 * different entry per player. Names are case-insensitive: the evaluated parts are lowercased like the written ones.
 */
public final class VariableExpression implements Expression, Changeable {
    private static final String LIST_SUFFIX = "::*";
    private static final Set<ChangeMode> SINGLE_MODES = EnumSet.of(ChangeMode.SET, ChangeMode.ADD, ChangeMode.REMOVE,
            ChangeMode.DELETE, ChangeMode.RESET);

    private final String source;
    private final VariableScope scope;
    private final List<Object> segments;
    private final boolean list;

    private VariableExpression(String source, VariableScope scope, List<Object> segments, boolean list) {
        this.source = source;
        this.scope = scope;
        this.segments = List.copyOf(segments);
        this.list = list;
    }

    public static VariableExpression parse(String token, ExpressionParser parser, ParseScope parseScope) {
        VariableScope.Parsed parsed = VariableScope.parse(token);
        String name = parsed.name();
        boolean list = name.endsWith(LIST_SUFFIX);
        if (list) {
            name = name.substring(0, name.length() - LIST_SUFFIX.length());
        }
        if (name.isEmpty()) {
            throw new SyntaxException(Language.get("parse.empty-variable-name"));
        }
        return new VariableExpression(token, parsed.scope(), segments(name, parser, parseScope), list);
    }

    private static List<Object> segments(String name, ExpressionParser parser, ParseScope parseScope) {
        List<Object> segments = new ArrayList<>();
        StringBuilder plain = new StringBuilder();
        int i = 0;
        while (i < name.length()) {
            int end = name.charAt(i) == '%' ? TextScanner.closingPercent(name, i) : -1;
            if (end < 0) {
                plain.append(name.charAt(i));
                i++;
                continue;
            }
            String inner = name.substring(i + 1, end);
            List<Token> tokens;
            try {
                tokens = Tokenizer.tokenize(inner);
            } catch (TokenizeException error) {
                throw new SyntaxException(Language.format("parse.in-variable-name", error.getMessage()));
            }
            Expression part = parser.parse(tokens, List.of(SkType.OBJECT), parseScope)
                    .orElseThrow(() -> new SyntaxException(
                            Language.format("parse.unknown-expression-in-variable-name", inner.strip())));
            if (!plain.isEmpty()) {
                segments.add(plain.toString());
                plain.setLength(0);
            }
            segments.add(part);
            i = end + 1;
        }
        if (!plain.isEmpty()) {
            segments.add(plain.toString());
        }
        return segments;
    }

    /** The name with every %expression% part evaluated, without the ::* of a list. */
    public String name(Context context) {
        StringBuilder name = new StringBuilder();
        for (Object segment : segments) {
            if (segment instanceof Expression part) {
                name.append(Converters.toText(part.evaluate(context), context).toLowerCase(Locale.ROOT));
            } else {
                name.append(segment);
            }
        }
        return name.toString();
    }

    @Override
    public SkType type() {
        return SkType.OBJECT;
    }

    @Override
    public boolean isList() {
        return list;
    }

    @Override
    public Object evaluate(Context context) {
        return list ? context.getList(scope, name(context)) : context.getVariable(scope, name(context));
    }

    @Override
    public String changeName() {
        return source;
    }

    @Override
    public Set<ChangeMode> changeModes() {
        return list ? EnumSet.allOf(ChangeMode.class) : SINGLE_MODES;
    }

    @Override
    public boolean changesWithMany() {
        return true;
    }

    @Override
    public void change(Context context, ChangeMode mode, Object value) {
        String name = name(context);
        if (list) {
            changeList(context, name, mode, value);
            return;
        }
        switch (mode) {
            case SET -> context.setVariable(scope, name, value instanceof List<?> items ? List.copyOf(items) : value);
            case ADD -> context.setVariable(scope, name, number(context, name, mode) + operand(mode, value));
            case REMOVE -> context.setVariable(scope, name, number(context, name, mode) - operand(mode, value));
            case DELETE, RESET, REMOVE_ALL -> context.deleteVariable(scope, name);
        }
    }

    private void changeList(Context context, String name, ChangeMode mode, Object value) {
        switch (mode) {
            case SET -> {
                context.deleteList(scope, name);
                List<Object> values = values(value);
                for (int i = 0; i < values.size(); i++) {
                    context.setVariable(scope, entry(name, String.valueOf(i + 1)), values.get(i));
                }
            }
            case ADD -> {
                int next = 1;
                for (Object item : values(value)) {
                    while (context.getVariable(scope, entry(name, String.valueOf(next))) != None.NONE) {
                        next++;
                    }
                    context.setVariable(scope, entry(name, String.valueOf(next)), item);
                }
            }
            case REMOVE, REMOVE_ALL -> remove(context, name, values(value), mode == ChangeMode.REMOVE_ALL);
            case DELETE, RESET -> context.deleteList(scope, name);
        }
    }

    private void remove(Context context, String name, List<Object> unwanted, boolean all) {
        IndexedValues entries = context.getList(scope, name);
        List<String> doomed = new ArrayList<>();
        for (Object item : unwanted) {
            for (int i = 0; i < entries.size(); i++) {
                String index = entries.index(i);
                if (!doomed.contains(index) && Comparators.test(Relation.EQUAL, entries.get(i), item)) {
                    doomed.add(index);
                    if (!all) {
                        break;
                    }
                }
            }
        }
        for (String index : doomed) {
            context.deleteVariable(scope, entry(name, index));
        }
    }

    private static String entry(String list, String index) {
        return list + "::" + index;
    }

    private static List<Object> values(Object value) {
        List<Object> values = new ArrayList<>();
        if (value instanceof List<?> items) {
            for (Object item : items) {
                if (item != None.NONE) {
                    values.add(item);
                }
            }
        } else if (value != null && value != None.NONE) {
            values.add(value);
        }
        return values;
    }

    private static double operand(ChangeMode mode, Object operand) {
        if (operand instanceof Double number) {
            return number;
        }
        String type = Converters.typeName(Converters.typeOf(operand));
        throw new ScriptError(Language.format(mode == ChangeMode.ADD ? "runtime.cannot-add-to-number"
                : "runtime.cannot-remove-from-number", type));
    }

    private double number(Context context, String name, ChangeMode mode) {
        Object existing = context.getVariable(scope, name);
        if (existing == None.NONE) {
            return 0.0;
        }
        if (existing instanceof Double number) {
            return number;
        }
        String type = Converters.typeName(Converters.typeOf(existing));
        throw new ScriptError(Language.format(mode == ChangeMode.ADD ? "runtime.cannot-add-number-to"
                : "runtime.cannot-remove-number-from", type));
    }

    @Override
    public String toString() {
        return source;
    }
}
