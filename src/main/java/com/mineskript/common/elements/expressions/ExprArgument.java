package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.ScriptCommand;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Name("Argument")
@Description({
        "A value typed after one of your script's commands, like Skript's arg. arg-1 (or arg 1, argument 1, the 1st argument) is the first one, arg-2 the second and so on, the last arg is the last one, and arguments is all of them as a list. When the command has only one argument it can be called just arg or the argument.",
        "Each argument has the type written in the command, so with command /boost <number> the arg is a number. An optional argument that was left out is none, or its default if it has one. Only available inside a command's trigger."
})
@Examples({
        "command /add <number> <number>:",
        "	trigger:",
        "		send \"%arg-1 + arg-2%\"",
        "",
        "command /greet [<text=friend>]:",
        "	trigger:",
        "		send \"hello %arg%\""
})
@Since("1.0.0-alpha.13")
public final class ExprArgument implements Expression {
    private static final String[] ORDINALS = {"(1st|first)", "(2nd|second)", "(3rd|third)", "(4th|fourth)", "(5th|fifth)",
            "(6th|sixth)", "(7th|seventh)", "(8th|eighth)", "(9th|ninth)"};

    private final int index;
    private final Expression position;
    private final SkType type;
    private final boolean all;
    private final boolean plural;

    private ExprArgument(int index, Expression position, SkType type, boolean all, boolean plural) {
        this.index = index;
        this.position = position;
        this.type = type;
        this.all = all;
        this.plural = plural;
    }

    public static void register(SyntaxRegistry registry) {
        StringBuilder numbered = new StringBuilder();
        StringBuilder ordinal = new StringBuilder();
        for (int i = 1; i <= ORDINALS.length; i++) {
            numbered.append(i == 1 ? "" : "|").append(i).append(":(arg-").append(i).append("|argument-").append(i).append(')');
            ordinal.append(i == 1 ? "" : "|").append(i).append(':').append(ORDINALS[i - 1]);
        }
        registry.addExpression(SkType.OBJECT, Tier.SIMPLE, ExprArgument::create,
                "[the] last (arg|argument)",
                "[the] (" + numbered + ")",
                "[the] (arg|argument) %number%",
                "[the] (" + ordinal + ") (arg|argument)",
                "[the] (arg|argument)",
                "[the] (args|arguments)");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (!(scope.event() instanceof Event.Command command)) {
            throw new SyntaxException("arguments are only available inside a command's trigger");
        }
        List<ScriptCommand.Argument> arguments = command.command().arguments();
        if (arguments.isEmpty()) {
            throw new SyntaxException("/" + command.command().name() + " has no arguments");
        }
        return switch (match.patternIndex()) {
            case 0 -> single(arguments, arguments.size());
            case 1, 3 -> single(arguments, tagged(match));
            case 2 -> {
                Expression position = match.slot(0);
                if (position instanceof ConstantExpression constant && constant.value() instanceof Double number) {
                    yield single(arguments, (int) Math.round(number));
                }
                yield Optional.of(new ExprArgument(0, position, SkType.OBJECT, false, false));
            }
            case 4 -> {
                if (arguments.size() > 1) {
                    throw new SyntaxException("/" + command.command().name()
                            + " has more than one argument, so say which one: arg-1, arg-2 and so on");
                }
                yield single(arguments, 1);
            }
            default -> Optional.of(new ExprArgument(0, null, commonType(arguments), true, true));
        };
    }

    private static int tagged(Match match) {
        for (int i = 1; i <= ORDINALS.length; i++) {
            if (match.has(String.valueOf(i))) {
                return i;
            }
        }
        return 0;
    }

    private static Optional<Expression> single(List<ScriptCommand.Argument> arguments, int number) {
        if (number < 1 || number > arguments.size()) {
            throw new SyntaxException("there is no argument " + number + ", the command has " + arguments.size());
        }
        ScriptCommand.Argument argument = arguments.get(number - 1);
        return Optional.of(new ExprArgument(number, null, argument.type(), false, argument.plural()));
    }

    private static SkType commonType(List<ScriptCommand.Argument> arguments) {
        SkType type = arguments.get(0).type();
        for (ScriptCommand.Argument argument : arguments) {
            if (argument.type() != type) {
                return SkType.OBJECT;
            }
        }
        return type;
    }

    @Override
    public SkType type() {
        return type;
    }

    @Override
    public boolean isList() {
        return plural;
    }

    @Override
    public Object evaluate(Context context) {
        if (!(context.eventValueOrNone(ScriptCommand.ARGUMENTS) instanceof List<?> values)) {
            return all ? List.of() : None.NONE;
        }
        if (all) {
            List<Object> flat = new ArrayList<>();
            for (Object value : values) {
                if (value instanceof List<?> list) {
                    flat.addAll(list);
                } else if (value != None.NONE) {
                    flat.add(value);
                }
            }
            return flat;
        }
        int number = index;
        if (position != null && position.evaluate(context) instanceof Double at) {
            number = (int) Math.round(at);
        }
        Object value = number >= 1 && number <= values.size() ? values.get(number - 1) : None.NONE;
        return plural && value == None.NONE ? List.of() : value;
    }
}
