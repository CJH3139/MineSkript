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

@Name("Max Health")
@Description("Your maximum health as a decimal number, in half hearts. It is normally 20 (10 hearts) and changes with effects such as health boost or with attribute changes. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"h\":",
        "\tset {_missing} to max health of player - health of player",
        "\tsend \"missing %{_missing}% health\""
})
@Since("1.0.0-alpha")
public final class ExprMaxHealth implements Expression {
    private ExprMaxHealth() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, (match, scope) -> Optional.of(new ExprMaxHealth()),
                "[the] max health of %player%",
                "%player%'s max health");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().playerMaxHealth();
    }
}
