package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Effect Level")
@Description("The level of an active status effect on you, as a whole number: 1 for Speed I, 2 for Speed II, and 0 when you do not have the effect. The name can be a plain effect name like speed or jump boost (spaces become underscores) or a full id like minecraft:speed. Also written effect level of. Skript's form, tier of speed of player, takes the effect written out instead of as text (see Potion Effect Tier). Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on effect gain:",
        "	set {_lvl} to level of effect \"speed\"",
        "	send \"speed level %{_lvl}%\"",
        "",
        "every 1 second:",
        "	if effect level of \"poison\" is greater than 0:",
        "		show action bar \"poisoned\""
})
@Since("1.0.0-alpha.2")
public final class ExprEffectLevel implements Expression {
    private final Expression effect;

    private ExprEffectLevel(Expression effect) {
        this.effect = effect;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.COMBINED, Priority.after(Priority.SIMPLE),
                (match, scope) -> match.slot(0).isList() ? Optional.empty() : Optional.of(new ExprEffectLevel(match.slot(0))),
                "[the] level of effect %string%",
                "[the] effect level of %string%");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return (double) context.world().effectLevel(Converters.toText(effect.evaluate(context), context));
    }
}
