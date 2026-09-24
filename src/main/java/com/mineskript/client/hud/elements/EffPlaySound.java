package com.mineskript.client.hud.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Play Sound")
@Description({"Plays a sound for you alone, at full volume and normal pitch, using its sound event id such as \"minecraft:ui.button.click\" or \"entity.experience_orb.pickup\". The namespace is optional and defaults to minecraft.",
        "An unknown sound id is silently ignored rather than reported. Needs a world."})
@Examples({"on chat:",
        "\tif message contains \"%name of player%\":",
        "\t\tplay sound \"minecraft:block.note_block.pling\""})
@Since("1.0.0-alpha.2")
public final class EffPlaySound implements Statement {
    private final int line;
    private final Expression sound;

    private EffPlaySound(int line, Expression sound) {
        this.line = line;
        this.sound = sound;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList()
                        ? Optional.empty()
                        : Optional.of(new EffPlaySound(scope.line(), match.slot(0))),
                "play sound %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().playSound(Converters.toText(sound.evaluate(context), context));
        return Flow.CONTINUE;
    }
}
