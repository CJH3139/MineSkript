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
import java.util.Optional;

@Name("Parsed As Integer")
@Description("Turns text into a whole number. Text that is a number with a fraction, such as 2.5, returns none, as does text that is not a number at all. A whole value written with decimals, like 3.0, is accepted as 3.")
@Examples({
        "on chat send:",
        "	set {_slot} to message parsed as integer",
        "	if {_slot} is set:",
        "		select slot {_slot}"
})
@Since("1.0.0-alpha.5")
public final class ExprParsedAsInteger implements Expression {
    private final Expression text;

    private ExprParsedAsInteger(Expression text) {
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprParsedAsInteger::create, "%string% parsed as [an] integer");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprParsedAsInteger(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context).strip();
        Object number = ParsedNumbers.number(value);
        return number instanceof Double parsed && parsed == Math.rint(parsed) ? parsed : None.NONE;
    }
}
