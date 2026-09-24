package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Distance")
@Description({"The distance between two locations in blocks, measured in a straight line, as a decimal number. Anything with a location works, such as the player, an entity or a block.",
        "Two locations in different dimensions have no distance between them: the result is none, like in Skript, so a comparison such as is less than 20 is false."})
@Examples({"every 1 second:",
        "\tif {home} is set:",
        "\t\tif distance between player and {home} is less than 20:",
        "\t\t\tshow action bar \"almost home\"",
        "",
        "on key press of \"d\":",
        "\tsend \"%distance between location(0, 64, 0) and location(3, 68, 0)% blocks\""})
@Since("1.0.0-alpha.10")
public final class ExprDistance implements Expression {
    private final Expression first;
    private final Expression second;

    private ExprDistance(Expression first, Expression second) {
        this.first = first;
        this.second = second;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, ExprDistance::create,
                "[the] distance between %location% and %location%");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new ExprDistance(match.slot(0), match.slot(1)));
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        if (first.evaluate(context) instanceof Location from && second.evaluate(context) instanceof Location to
                && from.sameDimension(to)) {
            return from.distance(to);
        }
        return None.NONE;
    }
}
