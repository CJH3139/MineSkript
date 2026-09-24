package com.mineskript.client.world.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Biome")
@Description({
        "The biome at your feet as a namespaced id text such as minecraft:plains or minecraft:dark_forest. Needs a world: outside a world the line stops with a \"no world\" error.",
        "Like Skript, biome of a location, or a location's biome, gives the biome there instead, such as biome of target block or biome of {home}. Your game only knows the dimension you are in, so a location in another dimension has no biome: the result is none."
})
@Examples({
        "on key press of \"b\":",
        "	send \"you are in %biome%\"",
        "",
        "every 5 seconds:",
        "	if biome is \"minecraft:desert\":",
        "		show action bar \"bring water\"",
        "",
        "on key press of \"b\":",
        "	send \"the block you look at is in %biome of target block%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprBiome implements Expression {
    private final Expression location;

    private ExprBiome(Expression location) {
        this.location = location;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> create(match.slot(0)),
                "[the] biome [of %locations%]",
                "%locations%'s biome");
    }

    private static Optional<Expression> create(Expression location) {
        if (location != null && location.isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprBiome(location));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        if (location == null) {
            return context.world().biome();
        }
        Location at = Locations.read(location, context);
        if (!Locations.isHere(at, context)) {
            return None.NONE;
        }
        return context.world().biomeAt(at.x(), at.y(), at.z());
    }
}
