package com.mineskript.syntax.conditions;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondWithin implements Condition {
    private final Expression entity;
    private final Expression range;
    private final boolean negate;

    private CondWithin(Expression entity, Expression range, boolean negate) {
        this.entity = entity;
        this.range = range;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, false), "%entity% (is|are) within %number% (block|blocks)");
        registry.addCondition((match, scope) -> create(match, true),
                "%entity% (isn't|is not|aren't|are not) within %number% (block|blocks)");
        registry.addCondition((match, scope) -> point(match, false),
                "[the] (player|me|myself) (is|are) within %number% (block|blocks) of %number%, %number%, %number%");
        registry.addCondition((match, scope) -> point(match, true),
                "[the] (player|me|myself) (isn't|is not|aren't|are not) within %number% (block|blocks) of %number%, %number%, %number%");
    }

    private static Optional<Condition> point(Match match, boolean negate) {
        for (int i = 0; i < 4; i++) {
            if (match.slot(i).isList()) {
                return Optional.empty();
            }
        }
        Expression range = match.slot(0);
        Expression x = match.slot(1);
        Expression y = match.slot(2);
        Expression z = match.slot(3);
        return Optional.of(context -> {
            GameBridge world = context.world();
            double dx = world.playerX() - (Double) x.evaluate(context);
            double dy = world.playerY() - (Double) y.evaluate(context);
            double dz = world.playerZ() - (Double) z.evaluate(context);
            return negate != (Math.sqrt(dx * dx + dy * dy + dz * dz) <= (Double) range.evaluate(context));
        });
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression entity = match.slot(0);
        Expression range = match.slot(1);
        if (entity.isList() || range.isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondWithin(entity, range, negate));
    }

    @Override
    public boolean test(Context context) {
        Object value = entity.evaluate(context);
        boolean result = value instanceof EntityValue found && found.distance() <= (Double) range.evaluate(context);
        return negate != result;
    }
}
