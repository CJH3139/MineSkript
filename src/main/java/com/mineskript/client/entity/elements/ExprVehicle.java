package com.mineskript.client.entity.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Vehicle")
@Description("The entity you are riding, such as a horse, boat or minecart. Returns nothing (none) when you are not riding anything, which prints as <none>. The value is a snapshot taken when it is read. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on mount:",
        "\tsend \"riding %name of vehicle% (%id of vehicle%)\"",
        "",
        "on key press of \"v\":",
        "\tif vehicle is set:",
        "\t\tsend \"riding %vehicle%\"",
        "\telse:",
        "\t\tsend \"on foot\""
})
@Since("1.0.0-alpha.2")
public final class ExprVehicle implements Expression {
    private ExprVehicle() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ENTITY, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprVehicle()), "[the] vehicle");
    }

    @Override
    public SkType type() {
        return SkType.ENTITY;
    }

    @Override
    public Object evaluate(Context context) {
        Object vehicle = context.world().vehicle();
        return vehicle == null ? None.NONE : vehicle;
    }
}
