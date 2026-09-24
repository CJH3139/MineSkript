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

@Name("Online Player Names")
@Description({
        "A list of the names of every player in the tab list, sorted alphabetically, yourself included. Use it with loop, is in, or contains; printed directly it reads as a list like Alex, Notch and Steve. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Players hidden from the tab list are not included. Compare with players online, which counts differently."
})
@Examples({
        "on key press of \"p\":",
        "	loop online player names:",
        "		send \"online: %loop-value%\"",
        "",
        "on tab list change:",
        "	if online player names contains \"Notch\":",
        "		show title \"Notch is here\""
})
@Since("1.0.0-alpha.2")
public final class ExprOnlinePlayerNames implements Expression {
    private ExprOnlinePlayerNames() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprOnlinePlayerNames()), "[the] online player (name|names)");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().onlinePlayerNames();
    }
}
