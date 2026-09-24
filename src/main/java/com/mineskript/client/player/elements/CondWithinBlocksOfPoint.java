package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Is Within Blocks Of Point")
@Description({
        "Checks whether you are within a number of blocks of the coordinates x, y, z, measured in a straight line from your feet. The range is inclusive.",
        "Only player, me or myself can be the subject of this form."
})
@Examples({
        "every 1 second:",
        "	if player is within 5 blocks of 100, 64, -200:",
        "		show action bar \"you are at the base\"",
        "",
        "on move:",
        "	if {-announced} is not set:",
        "		if me is within 3 blocks of 0, 70, 0:",
        "			set {-announced} to true",
        "			send \"welcome to spawn\"",
        "",
        "every 1 second:",
        "	if player is not within 50 blocks of 0, 64, 0:",
        "		show action bar \"outside the spawn area\""
})
@Since("1.0.0-alpha.5")
public final class CondWithinBlocksOfPoint implements Condition {
    private final Expression range;
    private final Expression x;
    private final Expression y;
    private final Expression z;
    private final boolean negate;

    private CondWithinBlocksOfPoint(Expression range, Expression x, Expression y, Expression z, boolean negate) {
        this.range = range;
        this.x = x;
        this.y = y;
        this.z = z;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "[the] (player|me|myself) (is|are) within %number% (block|blocks) of %number%, %number%, %number%",
                "[the] (player|me|myself) (isn't|is not|aren't|are not) within %number% (block|blocks) of %number%, %number%, %number%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        for (int i = 0; i < 4; i++) {
            if (match.slot(i).isList()) {
                return Optional.empty();
            }
        }
        return Optional.of(new CondWithinBlocksOfPoint(match.slot(0), match.slot(1), match.slot(2), match.slot(3), negate));
    }

    @Override
    public boolean test(Context context) {
        GameBridge world = context.world();
        double dx = world.playerX() - (Double) x.evaluate(context);
        double dy = world.playerY() - (Double) y.evaluate(context);
        double dz = world.playerZ() - (Double) z.evaluate(context);
        return negate != (Math.sqrt(dx * dx + dy * dy + dz * dz) <= (Double) range.evaluate(context));
    }
}
