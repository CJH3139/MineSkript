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

@Name("Server Brand")
@Description("The brand the server reports, such as vanilla or Paper (the text the F3 screen shows). Returns empty text when you are not connected or the server has not sent one. Works without a world.")
@Examples({
        "on world join:",
        "	wait 2 seconds",
        "	send \"server software: %server brand%\""
})
@Since("1.0.0-alpha.2")
public final class ExprServerBrand implements Expression {
    private ExprServerBrand() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprServerBrand()), "[the] server brand");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.game().serverBrand();
    }
}
