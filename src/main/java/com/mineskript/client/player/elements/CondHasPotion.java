package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.PotionEffectType;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Has Potion")
@Description({
        "Checks whether you have any potion effect, or particular ones, like Skript's has potion. Effects are written by name, such as speed, night vision, fire resistance or jump boost (Skript's other names like swiftness or fast digging work too), or as an id such as minecraft:speed; effects from mods need their namespace. The level does not matter.",
        "With several effects joined by and, all of them must be active; joined by or, one is enough. It always reads your own player."
})
@Examples({
        "on key press of \"h\":",
        "\tif player has potion speed:",
        "\t\tsend \"you are fast\"",
        "",
        "every 5 seconds:",
        "\tif player doesn't have any potion effects:",
        "\t\tshow action bar \"no effects\"",
        "",
        "on key press of \"n\":",
        "\tif player has potion effects night vision and water breathing:",
        "\t\tsend \"ready to dive\""
})
@Since("1.0.0-alpha.11")
public final class CondHasPotion implements Condition {
    private static final String HAVE = "(has|have)";
    private static final String NOT_HAVE = "(doesn't|does not|do not|don't) have";
    private static final String ANY = "[any] (potion effect|potion effects)";
    private static final String NAMED = "(potion|potions) [(effect|effects)] %potioneffecttypes%";

    private final Expression effects;
    private final boolean negate;

    private CondHasPotion(Expression effects, boolean negate) {
        this.effects = effects;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() >= 2),
                "%players% " + HAVE + " " + ANY,
                "%players% " + HAVE + " " + NAMED,
                "%players% " + NOT_HAVE + " " + ANY,
                "%players% " + NOT_HAVE + " " + NAMED);
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        Expression effects = match.slots().size() > 1 ? match.slot(1) : null;
        return Optional.of(new CondHasPotion(effects, negate));
    }

    @Override
    public boolean test(Context context) {
        if (effects == null) {
            return negate == context.world().activeEffects().isEmpty();
        }
        Object value = effects.evaluate(context);
        List<?> wanted = value instanceof List<?> list ? list : List.of(value);
        boolean any = effects instanceof ListExpression list && list.disjunctive();
        boolean result = any
                ? wanted.stream().anyMatch(effect -> active(effect, context))
                : !wanted.isEmpty() && wanted.stream().allMatch(effect -> active(effect, context));
        return negate != result;
    }

    private static boolean active(Object effect, Context context) {
        return effect instanceof PotionEffectType type && context.world().activeEffects().containsKey(type.id());
    }
}
