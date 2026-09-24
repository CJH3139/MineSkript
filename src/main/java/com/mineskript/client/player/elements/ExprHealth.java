package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.PastState;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Health")
@Description("Your current health as a decimal number, in half hearts: 20 is a full bar of 10 hearts and 0 is dead. Written health of player, player's health or just health, like Skript. In on damage, on heal and on health change, past health is the health before the change (see Former State). Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "every 1 second:",
        "	if health of player is less than 6:",
        "		show title \"low health\"",
        "",
        "on damage:",
        "	send \"health now %player's health% of %max health of player%\""
})
@Since({"1.0.0-alpha", "1.0.0-alpha.11"})
public final class ExprHealth implements Expression, PastState {
    private ExprHealth() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, (match, scope) -> Optional.of(new ExprHealth()),
                "[the] health [of %player%]",
                "%player%'s health");
    }

    @Override
    public String pastEventValue() {
        return "old health";
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().playerHealth();
    }
}
