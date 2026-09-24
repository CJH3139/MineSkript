package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.PastState;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.AmbiguousExpression;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Former State")
@Description({
        "The value something had just before the event, like Skript's past state: past health is your health before it changed, and past held item (or former tool) the item in the hotbar slot you switched away from. Written past, former or old, as in past health, former state of held item or held item before the event.",
        "Only some values have a past state, and only in the events that change them: health in on damage, on heal and on health change, and the held item in on held item change. Anywhere else it is a parse error. Without past, the value is the one after the event."
})
@Examples({
        "on damage:",
        "\tsend \"health went from %past health% to %health%\"",
        "",
        "on held item change:",
        "\tsend \"put away %former held item%\""
})
@Since("1.0.0-alpha.11")
public final class ExprTimeState implements Expression {
    private final Expression value;

    private ExprTimeState(Expression value) {
        this.value = value;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.OBJECT, Tier.PROPERTY, ExprTimeState::create,
                "[the] (former|past|old) [state] [of] %objects%",
                "%objects% before [the event]");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        Expression inner = unwrapped(match.slot(0));
        if (!(inner instanceof PastState past)) {
            return Optional.empty();
        }
        String name = past.pastEventValue();
        Event event = scope.event();
        if (event == null || !event.context().provides(name)) {
            throw new SyntaxException("this value has no past state in this event: past states only exist in the"
                    + " events that change the value, such as past health in on damage");
        }
        return Optional.of(new ExprTimeState(ExprEventValue.reading(name, inner.type())));
    }

    private static Expression unwrapped(Expression expression) {
        Expression current = AmbiguousExpression.primary(expression);
        while (current instanceof ConvertedExpression converted) {
            current = AmbiguousExpression.primary(converted.inner());
        }
        return current;
    }

    @Override
    public SkType type() {
        return value.type();
    }

    @Override
    public Object evaluate(Context context) {
        return value.evaluate(context);
    }
}
