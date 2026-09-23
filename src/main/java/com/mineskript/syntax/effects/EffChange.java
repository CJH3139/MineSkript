package com.mineskript.syntax.effects;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.VariableExpression;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;

public final class EffChange implements Statement {
    private enum Mode {
        SET,
        ADD,
        REMOVE,
        DELETE
    }

    private final int line;
    private final Mode mode;
    private final VariableExpression target;
    private final Expression value;

    private EffChange(int line, Mode mode, VariableExpression target, Expression value) {
        this.line = line;
        this.mode = mode;
        this.target = target;
        this.value = value;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> create(scope, Mode.SET, match.slot(0), match.slot(1)), "set %objects% to %objects%");
        registry.addEffect((match, scope) -> create(scope, Mode.ADD, match.slot(1), match.slot(0)), "(add|give) %objects% to %objects%");
        registry.addEffect((match, scope) -> create(scope, Mode.ADD, match.slot(0), match.slot(1)), "increase %objects% by %objects%");
        registry.addEffect((match, scope) -> create(scope, Mode.REMOVE, match.slot(1), match.slot(0)), "(remove|subtract) %objects% from %objects%");
        registry.addEffect((match, scope) -> create(scope, Mode.REMOVE, match.slot(0), match.slot(1)), "(reduce|decrease) %objects% by %objects%");
        registry.addEffect((match, scope) -> create(scope, Mode.DELETE, match.slot(0), null), "(delete|clear) %objects%");
    }

    private static Optional<Statement> create(ParseScope scope, Mode mode, Expression target, Expression value) {
        if (!(target instanceof VariableExpression variable)) {
            throw new SyntaxException("can only set variables");
        }
        return Optional.of(new EffChange(scope.line(), mode, variable, value));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        switch (mode) {
            case SET -> context.setVariable(target.scope(), target.name(), value.evaluate(context));
            case DELETE -> context.deleteVariable(target.scope(), target.name());
            case ADD -> context.setVariable(target.scope(), target.name(), current(context) + operand(context));
            case REMOVE -> context.setVariable(target.scope(), target.name(), current(context) - operand(context));
        }
        return Flow.CONTINUE;
    }

    private double operand(Context context) {
        Object operand = value.evaluate(context);
        if (operand instanceof Double number) {
            return number;
        }
        String verb = mode == Mode.ADD ? "add" : "remove";
        String preposition = mode == Mode.ADD ? "to" : "from";
        throw new ScriptError("cannot " + verb + " " + Converters.typeName(Converters.typeOf(operand)) + " " + preposition + " a number");
    }

    private double current(Context context) {
        Object existing = context.getVariable(target.scope(), target.name());
        if (existing == None.NONE) {
            return 0.0;
        }
        if (existing instanceof Double number) {
            return number;
        }
        String verb = mode == Mode.ADD ? "add" : "remove";
        String preposition = mode == Mode.ADD ? "to" : "from";
        throw new ScriptError("cannot " + verb + " a number " + preposition + " " + Converters.typeName(Converters.typeOf(existing)));
    }
}
