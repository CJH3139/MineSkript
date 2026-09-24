package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Is Within Blocks")
@Description("Checks whether an entity is no further than a number of blocks from you, measured in a straight line. Written with an entity expression such as nearest entity, nearest player or target entity. When there is no such entity it is false. The negated form is true when the entity is further away, and also when there is no entity at all.")
@Examples({
        "every 1 second:",
        "	if nearest player is within 10 blocks:",
        "		show action bar \"%name of nearest player% is nearby\"",
        "",
        "every 2 seconds:",
        "	if nearest entity is not within 16 blocks:",
        "		show action bar \"nothing around\""
})
@Since("1.0.0-alpha.2")
public final class CondWithinBlocks implements Condition {
    private final Expression entity;
    private final Expression range;
    private final boolean negate;

    private CondWithinBlocks(Expression entity, Expression range, boolean negate) {
        this.entity = entity;
        this.range = range;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "%entity% (is|are) within %number% (block|blocks)",
                "%entity% (isn't|is not|aren't|are not) within %number% (block|blocks)");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression entity = match.slot(0);
        Expression range = match.slot(1);
        if (entity.isList() || range.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondWithinBlocks(entity, range, negate));
    }

    @Override
    public boolean test(Context context) {
        Object value = entity.evaluate(context);
        boolean result = value instanceof EntityValue found && found.distance() <= (Double) range.evaluate(context);
        return negate != result;
    }
}
