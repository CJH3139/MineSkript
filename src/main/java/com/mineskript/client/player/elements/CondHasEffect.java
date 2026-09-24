package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Has Effect")
@Description({
        "Checks whether you currently have a status effect at any level. The name is matched against the effect id, with spaces turned into underscores and minecraft: added when no namespace is given, so \"speed\", \"fire resistance\" and \"minecraft:fire_resistance\" all work. It always reads your own player.",
        "An unknown effect name is not an error; it is simply never active. Use level of effect to read the level. Skript's form, player has potion speed, takes the effect written out instead of as text (see Has Potion)."
})
@Examples({
        "on key press of \"h\":",
        "	if player has effect \"fire resistance\":",
        "		send \"safe to swim in lava\"",
        "	else:",
        "		send \"no fire resistance\"",
        "",
        "every 30 seconds:",
        "	if player doesn't have effect \"night vision\":",
        "		show action bar \"night vision ran out\""
})
@Since("1.0.0-alpha.2")
public final class CondHasEffect implements Condition {
    private final Expression effect;
    private final boolean negate;

    private CondHasEffect(Expression effect, boolean negate) {
        this.effect = effect;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "%player% (has|have) [the] effect %string%",
                "%player% (doesn't have|does not have|don't have|do not have) [the] effect %string%");
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        if (match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new CondHasEffect(match.slot(1), negate));
    }

    @Override
    public boolean test(Context context) {
        String name = Converters.toText(effect.evaluate(context), context);
        return negate != (context.world().effectLevel(name) > 0);
    }
}
