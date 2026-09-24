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
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

@Name("Parsed As Boolean")
@Description("Turns text into true or false. true, yes and on give true; false, no and off give false; case and surrounding spaces are ignored. Anything else returns none.")
@Examples({
        "on chat send:",
        "	set {_answer} to message parsed as boolean",
        "	if {_answer} is true:",
        "		send \"agreed\""
})
@Since("1.0.0-alpha.5")
public final class ExprParsedAsBoolean implements Expression {
    private final Expression text;

    private ExprParsedAsBoolean(Expression text) {
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.BOOLEAN, Tier.COMBINED, ExprParsedAsBoolean::create, "%string% parsed as [a] boolean");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprParsedAsBoolean(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.BOOLEAN;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context).strip();
        Object result = switch (value.toLowerCase(Locale.ROOT)) {
            case "true", "yes", "on" -> Boolean.TRUE;
            case "false", "no", "off" -> Boolean.FALSE;
            default -> None.NONE;
        };
        context.setEventValue(ExprParse.ERROR, result == None.NONE ? value + " could not be parsed as a boolean" : None.NONE);
        return result;
    }
}
