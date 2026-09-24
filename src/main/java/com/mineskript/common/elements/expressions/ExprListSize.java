package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.parse.VariableExpression;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Size Of List")
@Description({
        "How many values a list has, as a whole number: a list variable such as {homes::*}, a split text, online player names or a list written with commas. Also written amount of or number of. An empty or unset list gives 0.",
        "A single variable also works: it counts the entries when it holds a list, 1 when it holds one value and 0 when it is unset. For a single item, amount of still means the number of items in the stack, and number of X in the inventory still counts items in your inventory."
})
@Examples({
        "on key press of \"l\":",
        "	send \"%size of {homes::*}% homes saved\"",
        "",
        "on chat:",
        "	set {_words::*} to message split at \" \"",
        "	if number of {_words::*} is greater than 20:",
        "		send \"that was a long message\""
})
@Since("1.0.0-alpha.8")
public final class ExprListSize implements Expression {
    private final Expression list;

    private ExprListSize(Expression list) {
        this.list = list;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprListSize::create, "[the] (size|amount|number) of %objects%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        Expression list = match.slot(0);
        if (!list.isList() && !(list instanceof VariableExpression)) {
            return Optional.empty();
        }
        return Optional.of(new ExprListSize(list));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        Object value = list.evaluate(context);
        if (value instanceof List<?> items) {
            return (double) items.stream().filter(item -> item != None.NONE).count();
        }
        return value == None.NONE ? 0.0 : 1.0;
    }
}
