package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Location Of")
@Description({"The location of the player, an entity or a block: an x, y and z position together with the dimension it is in. Also written position of, or player's location.",
        "For the player it is the position of your feet, in the dimension you are in. For an entity it is its position when the entity value was read (the bottom of the entity). For a block, such as target block or block at, it is the block's corner with the lowest x, y and z, so the location of the block at location(10.7, 64, -3.2) is x: 10, y: 64, z: -4.",
        "A block whose position is not known, such as the target block when you are not looking at one, stops the line with an error. A location in text reads like x: 1, y: 64, z: -3 in minecraft:overworld."})
@Examples({"on key press of \"h\":",
        "\tset {home} to location of player",
        "\tsend \"home set at %{home}%\"",
        "",
        "on key press of \"i\":",
        "\tif target block is not air:",
        "\t\tsend \"that block is at %target block's location%\""})
@Since("1.0.0-alpha.10")
public final class ExprLocationOf implements Expression {
    private final Expression source;

    private ExprLocationOf(Expression source) {
        this.source = source;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.LOCATION, Tier.PROPERTY, ExprLocationOf::create,
                "[the] (location|position) of %location%",
                "%location%'s (location|position)");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprLocationOf(match.slot(0)));
    }

    @Override
    public SkType type() {
        return SkType.LOCATION;
    }

    @Override
    public Object evaluate(Context context) {
        return source.evaluate(context);
    }
}
