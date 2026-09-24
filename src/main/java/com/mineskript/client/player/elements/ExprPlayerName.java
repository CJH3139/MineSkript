package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Player Name")
@Description("Your own username as text. Written name of player or player's name. Printing player in text gives the same result. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on world join:",
        "	send \"welcome back, %name of player%\""
})
@Since("1.0.0-alpha")
public final class ExprPlayerName implements Expression {
    private ExprPlayerName() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY, Priority.before(Priority.COMBINED),
                (match, scope) -> Optional.of(new ExprPlayerName()),
                "[the] name of %player%",
                "%player%'s name");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().playerName();
    }
}
