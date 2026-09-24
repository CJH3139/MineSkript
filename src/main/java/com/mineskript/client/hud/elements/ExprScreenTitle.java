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

@Name("Screen Title")
@Description("The title of the screen that is open, such as Chest, Crafting or a custom container name set by the server. Returns empty text when no screen is open. Also written open screen title or title of the current screen. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on screen open:",
        "	send \"opened: %screen title%\"",
        "",
        "every 10 ticks:",
        "	if screen title is \"Auction House\":",
        "		show action bar \"auction open\""
})
@Since("1.0.0-alpha.5")
public final class ExprScreenTitle implements Expression {
    private ExprScreenTitle() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprScreenTitle()),
                "[the] [(current|open)] screen title", "[the] title of [the] [(current|open)] screen");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().screenTitle();
    }
}
