package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.PotionEffectType;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;

@Name("Potion Effect Tier")
@Description({
        "The tier of one of your potion effects, like Skript's potion effect tier: 1 for Speed I, 2 for Speed II, and 0 when you do not have the effect. Also written amplifier of or level of. The effect is written by name, such as haste or jump boost, not as text. It always reads your own player, so the player after of must be player, me or myself.",
        "Effect Level does the same with the effect name as text, such as level of effect \"speed\"."
})
@Examples({
        "every 1 second:",
        "\tif the amplifier of haste of player is at least 2:",
        "\t\tshow action bar \"mining fast\"",
        "",
        "on effect gain:",
        "\tsend \"speed tier %tier of speed of player%\""
})
@Since("1.0.0-alpha.11")
public final class ExprPotionEffectTier implements Expression {
    private final Expression effect;

    private ExprPotionEffectTier(Expression effect) {
        this.effect = effect;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED,
                (match, scope) -> match.slot(0).isList() ? Optional.empty()
                        : Optional.of(new ExprPotionEffectTier(match.slot(0))),
                "[the] [potion] (tier|amplifier|level) of %potioneffecttypes% (of|for|on) %players%");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        if (!(effect.evaluate(context) instanceof PotionEffectType type)) {
            throw new ScriptError("there is no potion effect type");
        }
        return (double) context.world().activeEffects().getOrDefault(type.id(), 0);
    }
}
