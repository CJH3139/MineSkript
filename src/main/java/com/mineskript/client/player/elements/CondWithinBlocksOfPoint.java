package com.mineskript.client.player.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Is Within Blocks Of Location")
@Description({
        "Checks whether you are within a number of blocks of a location, measured in a straight line from your feet. The range is inclusive. The location can be written with location(x, y, z), a saved variable, an entity or a block.",
        "Only player, me or myself can be the subject of this form. A location in another dimension than yours is never within range, so the negated form is true for it."
})
@Examples({
        "every 1 second:",
        "	if player is within 5 blocks of location(100, 64, -200):",
        "		show action bar \"you are at the base\"",
        "",
        "on move:",
        "	if {-announced} is not set:",
        "		if me is within 3 blocks of location(0, 70, 0):",
        "			set {-announced} to true",
        "			send \"welcome to spawn\"",
        "",
        "every 1 second:",
        "	if {home} is set:",
        "		if player is not within 50 blocks of {home}:",
        "			show action bar \"far from home\""
})
@Since("1.0.0-alpha.5, 1.0.0-alpha.10 (locations)")
public final class CondWithinBlocksOfPoint implements Condition {
    private final Expression range;
    private final Expression target;
    private final boolean negate;

    private CondWithinBlocksOfPoint(Expression range, Expression target, boolean negate) {
        this.range = range;
        this.target = target;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "[the] (player|me|myself) (is|are) within %number% (block|blocks) of %location%",
                "[the] (player|me|myself) (isn't|is not|aren't|are not) within %number% (block|blocks) of %location%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondWithinBlocksOfPoint(match.slot(0), match.slot(1), negate));
    }

    @Override
    public boolean test(Context context) {
        GameBridge world = context.world();
        Location location = Locations.read(target, context);
        if (!Locations.isHere(location, context)) {
            return negate;
        }
        double dx = world.playerX() - location.x();
        double dy = world.playerY() - location.y();
        double dz = world.playerZ() - location.z();
        return negate != (Math.sqrt(dx * dx + dy * dy + dz * dz) <= (Double) range.evaluate(context));
    }
}
