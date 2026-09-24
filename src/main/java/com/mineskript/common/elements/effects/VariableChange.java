package com.mineskript.common.elements.effects;

import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.List;
import java.util.Optional;

final class VariableChange implements Statement {
    private final int line;
    private final ChangeMode mode;
    private final Changeable target;
    private final Expression value;

    private VariableChange(int line, ChangeMode mode, Changeable target, Expression value) {
        this.line = line;
        this.mode = mode;
        this.target = target;
        this.value = value;
    }

    static Optional<Statement> create(ParseScope scope, ChangeMode mode, Expression target, Expression value) {
        Changeable changer = target.changer().orElseThrow(() -> new SyntaxException("can only set variables"));
        if (!changer.changeModes().contains(mode)) {
            throw new SyntaxException(refusal(changer, mode));
        }
        Expression checked = value == null ? null : checked(changer, mode, value);
        return Optional.of(new VariableChange(scope.line(), mode, changer, checked));
    }

    private static String refusal(Changeable changer, ChangeMode mode) {
        List<String> allowed = changer.changeModes().stream().map(ChangeMode::phrase).toList();
        if (allowed.isEmpty()) {
            return changer.changeName() + " cannot be changed";
        }
        String list = allowed.size() == 1
                ? allowed.get(0)
                : String.join(", ", allowed.subList(0, allowed.size() - 1)) + " or " + allowed.getLast();
        return changer.changeName() + " can only be " + list + ", not " + mode.phrase();
    }

    private static Expression checked(Changeable changer, ChangeMode mode, Expression value) {
        if (value.isList() && !changer.changesWithMany()) {
            throw new SyntaxException(mode == ChangeMode.SET
                    ? changer.changeName() + " can only be set to one value"
                    : "only one value can be " + mode.phrase() + " " + changer.changeName());
        }
        SkType wanted = changer.changeType(mode);
        if (wanted == SkType.OBJECT || wanted == value.type()) {
            return value;
        }
        if (value.type() == SkType.OBJECT || Converters.canConvert(value.type(), wanted)) {
            return new ConvertedExpression(value, wanted);
        }
        String type = Converters.typeName(value.type());
        throw new SyntaxException(switch (mode) {
            case SET -> "cannot set " + changer.changeName() + " to " + type;
            case ADD -> "cannot add " + type + " to " + changer.changeName();
            default -> "cannot remove " + type + " from " + changer.changeName();
        });
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        target.change(context, mode, value == null ? None.NONE : value.evaluate(context));
        return Flow.CONTINUE;
    }
}
