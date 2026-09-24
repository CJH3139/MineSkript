package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Parse Error")
@Description("Why the last parse in this trigger failed, such as abc could not be parsed as a number, like Skript's parse error. It is none when the last parse worked or nothing has been parsed yet.")
@Examples({
        "on chat send:",
        "	set {_n} to message parsed as number",
        "	if {_n} is not set:",
        "		send \"%parse error%\"",
        "",
        "on chat:",
        "	set {_parts::*} to message parsed as \"%string% joined the game\"",
        "	if the last parse error is not set:",
        "		send \"welcome %{_parts::1}%\""
})
@Since("1.0.0-alpha.13")
public final class ExprParseError implements Expression {
    private ExprParseError() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprParseError()),
                "[the] [last] [parse] error");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValueOrNone(ExprParse.ERROR);
    }
}
