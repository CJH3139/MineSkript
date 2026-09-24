package com.mineskript.client.hud.elements;

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

@Name("Screen Type")
@Description({
        "The kind of screen that is open, as the name of its Java class, such as InventoryScreen, ContainerScreen or ChatScreen. Returns empty text when no screen is open. Also written screen kind or type of the open screen. Needs a world: outside a world the line stops with a \"no world\" error.",
        "The exact names depend on the game version, so print them once to see what your screen is called."
})
@Examples({
        "on screen open:",
        "	send \"screen type: %screen type%\""
})
@Since("1.0.0-alpha.5")
public final class ExprScreenType implements Expression {
    private ExprScreenType() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprScreenType()),
                "[the] [(current|open)] screen (type|kind)", "[the] (type|kind) of [the] [(current|open)] screen");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().screenType();
    }
}
