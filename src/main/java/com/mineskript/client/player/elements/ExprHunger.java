package com.mineskript.client.player.elements;

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

@Name("Hunger")
@Description("Your food level as a whole number from 0 to 20, where 20 is a full hunger bar. Written like Skript's food level: food level, hunger, food bar or hunger meter, alone or as food level of player or player's hunger. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "every 5 seconds:",
        "\tif hunger of player is at most 6:",
        "\t\tsend \"eat something\"",
        "",
        "on hunger change:",
        "\tshow action bar \"food %food level%, %player's hunger bar% on the bar\""
})
@Since({"1.0.0-alpha", "1.0.0-alpha.11"})
public final class ExprHunger implements Expression {
    private ExprHunger() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, (match, scope) -> Optional.of(new ExprHunger()),
                "[the] (food|hunger) [(level|meter|metre|bar)] [of %players%]",
                "%players%'s (food|hunger) [(level|meter|metre|bar)]");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().playerHunger();
    }
}
