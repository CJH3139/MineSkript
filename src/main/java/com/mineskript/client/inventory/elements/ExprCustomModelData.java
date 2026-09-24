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
import java.util.List;
import java.util.Optional;

@Name("Custom Model Data")
@Description({"The custom model data number of an item, which servers use to give items their own look. Since Minecraft 1.21.4 an item can carry a list of numbers; this gives the first one, or none when the item has no custom model data numbers.",
        "Items read back from saved variables have no custom model data. If the value is not an item the line stops with a \"there is no item\" error."})
@Examples({"on key press of \"m\":",
        "\tif custom model data of held item is set:",
        "\t\tsend \"model %custom model data of held item%\""})
@Since("1.0.0-alpha.9")
public final class ExprCustomModelData extends ItemPropertyExpression {
    private ExprCustomModelData(Expression item) {
        super(SkType.NUMBER, value -> first(value.details().customModelData()), item);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprCustomModelData(match.slot(0))),
                "[the] custom model data of %item%",
                "%item%'s custom model data");
    }

    private static Object first(List<Double> numbers) {
        return numbers.isEmpty() ? None.NONE : numbers.getFirst();
    }
}
