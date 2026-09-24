package com.mineskript.client.world.elements;

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

@Name("Biome")
@Description("The biome at your feet as a namespaced id text such as minecraft:plains or minecraft:dark_forest. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"b\":",
        "\tsend \"you are in %biome%\"",
        "",
        "every 5 seconds:",
        "\tif biome is \"minecraft:desert\":",
        "\t\tshow action bar \"bring water\""
})
@Since("1.0.0-alpha.2")
public final class ExprBiome implements Expression {
    private ExprBiome() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprBiome()), "[the] biome");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.world().biome();
    }
}
