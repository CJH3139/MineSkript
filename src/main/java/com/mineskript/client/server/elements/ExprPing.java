package com.mineskript.client.server.elements;

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

@Name("Ping")
@Description("Your latency to the server in milliseconds, as a whole number, taken from your own tab list entry. Returns 0 when you are not connected or the server has not reported it yet (singleplayer usually shows 0). Works without a world.")
@Examples({
        "every 10 seconds:",
        "	if ping is greater than 300:",
        "		show action bar \"lag: %ping% ms\""
})
@Since("1.0.0-alpha.2")
public final class ExprPing implements Expression {
    private ExprPing() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprPing()), "[the] ping");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.game().ping();
    }
}
