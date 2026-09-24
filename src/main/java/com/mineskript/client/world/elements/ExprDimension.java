package com.mineskript.client.world.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Dimension")
@Description("The dimension you are in as a namespaced id text: minecraft:overworld, minecraft:the_nether or minecraft:the_end (or a modded or server-defined id). Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on dimension change:",
        "\tsend \"now in %dimension%\"",
        "",
        "on key press of \"d\":",
        "\tif dimension is \"minecraft:the_nether\":",
        "\t\tsend \"in the nether\""
})
@Since("1.0.0-alpha.2")
public final class ExprDimension extends GameValueExpression {
    private ExprDimension() {
        super(SkType.TEXT, GameBridge::dimension);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprDimension()), "[the] dimension");
    }
}
