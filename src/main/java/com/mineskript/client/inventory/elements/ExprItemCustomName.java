package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Item Custom Name")
@Description({"The name an item was given, in an anvil or by the server, as plain text without colours. An item that was never renamed has no custom name, so this gives none: check it with is set. Compare with name of, which gives the name the tooltip shows either way.",
        "Items read back from saved variables keep their name but not their other data, so they have no custom name. If the value is not an item the line stops with a \"there is no item\" error."})
@Examples({"on key press of \"n\":",
        "	if custom name of held item is set:",
        "		send \"renamed to %custom name of held item%\"",
        "	else:",
        "		send \"not renamed\""})
@Since("1.0.0-alpha.9")
public final class ExprItemCustomName extends ItemPropertyExpression {
    private ExprItemCustomName(Expression item) {
        super(SkType.TEXT, value -> value.details().customName().<Object>map(name -> name).orElse(None.NONE), item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemCustomName(match.slot(0))),
                "[the] custom name of %item%",
                "%item%'s custom name");
    }
}
