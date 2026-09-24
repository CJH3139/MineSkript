package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.AmbiguousExpression;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.VariableExpression;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.TextMatching;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Name("Replace")
@Description({
        "Replaces every occurrence of one or more texts inside a variable, a list variable or the message with another text, changing it in place, like Skript's replace effect. replace the first only replaces the first occurrence of each text.",
        "Matching is literal and ignores capitals, like Skript's default; add with case sensitivity to only replace text whose capitals match too. Only values that can be set to text can have parts replaced: variables, list variables (each text entry keeps its index) and the message of on chat send and on command send. Entries that are not text are left alone.",
        "To get a changed copy without changing anything, use the Replace Text expression: X with Y replaced with Z."
})
@Examples({
        "on chat send:",
        "\treplace all \"idiot\" and \"noob\" with \"****\" in the message",
        "",
        "on key press of \"r\":",
        "\tset {_msg} to \"<item> is ready\"",
        "\treplace \"<item>\" in {_msg} with \"%held item%\"",
        "\tsend {_msg}",
        "",
        "on key press of \"r\":",
        "\tset {_t} to \"a-b-c\"",
        "\treplace the first \"-\" with \"+\" in {_t}",
        "\treplace \"B\" with \"x\" in {_t} with case sensitivity"
})
@Since("1.0.0-alpha.11")
public final class EffReplace implements Statement {
    private final int line;
    private final Expression needles;
    private final List<Expression> targets;
    private final Expression replacement;
    private final boolean first;
    private final boolean caseSensitive;

    private EffReplace(int line, Expression needles, List<Expression> targets, Expression replacement, boolean first,
            boolean caseSensitive) {
        this.line = line;
        this.needles = needles;
        this.targets = targets;
        this.replacement = replacement;
        this.first = first;
        this.caseSensitive = caseSensitive;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffReplace::create,
                "replace [all|every|first:[the] first] %strings% in %strings% with %string% "
                        + "[case:with case sensitivity]",
                "replace [all|every|first:[the] first] %strings% with %string% in %strings% "
                        + "[case:with case sensitivity]");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        boolean inFirst = match.patternIndex() == 0;
        Expression haystack = match.slot(inFirst ? 1 : 2);
        Expression replacement = match.slot(inFirst ? 2 : 1);
        if (replacement.isList()) {
            return Optional.empty();
        }
        List<Expression> targets = new ArrayList<>();
        for (Expression target : parts(haystack)) {
            Expression unwrapped = unwrapped(target);
            if (!(unwrapped instanceof VariableExpression) && !settableToText(unwrapped)) {
                throw new SyntaxException(unwrapped.changer().map(Changeable::changeName).orElse("that value")
                        + " cannot be changed, so it cannot have parts replaced");
            }
            targets.add(unwrapped);
        }
        return Optional.of(new EffReplace(scope.line(), match.slot(0), List.copyOf(targets), replacement,
                match.has("first"), match.has("case")));
    }

    private static List<Expression> parts(Expression haystack) {
        Expression unwrapped = unwrapped(haystack);
        return unwrapped instanceof ListExpression list ? list.items() : List.of(unwrapped);
    }

    private static Expression unwrapped(Expression expression) {
        Expression current = AmbiguousExpression.primary(expression);
        while (current instanceof ConvertedExpression converted) {
            current = AmbiguousExpression.primary(converted.inner());
        }
        return current;
    }

    private static boolean settableToText(Expression target) {
        return target.changer()
                .filter(changer -> changer.changeModes().contains(ChangeMode.SET))
                .map(changer -> changer.changeType(ChangeMode.SET))
                .filter(type -> type == SkType.TEXT || type == SkType.OBJECT)
                .isPresent();
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        List<String> searched = texts(needles.evaluate(context), context);
        Object with = replacement.evaluate(context);
        if (searched.isEmpty() || with == None.NONE) {
            return Flow.CONTINUE;
        }
        String replacementText = Converters.toText(with, context);
        UnaryOperator<Object> change = value -> value instanceof String text
                ? replaced(text, searched, replacementText)
                : value;
        for (Expression target : targets) {
            if (target instanceof VariableExpression variable) {
                variable.changeInPlace(context, change);
            } else {
                Object value = target.evaluate(context);
                if (value instanceof String) {
                    target.changer().orElseThrow().change(context, ChangeMode.SET, change.apply(value));
                }
            }
        }
        return Flow.CONTINUE;
    }

    private String replaced(String text, List<String> searched, String replacementText) {
        String result = text;
        for (String needle : searched) {
            result = TextMatching.replace(result, needle, replacementText, caseSensitive, first);
        }
        return result;
    }

    private static List<String> texts(Object value, Context context) {
        List<String> texts = new ArrayList<>();
        for (Object item : value instanceof List<?> items ? items : List.of(value)) {
            if (item != None.NONE) {
                texts.add(Converters.toText(item, context));
            }
        }
        return texts;
    }
}
