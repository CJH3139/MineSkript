package com.mineskript.client.movement.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.KeyNames;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Press Key")
@Description({"Presses, holds or releases a keyboard key or mouse button as far as Minecraft's controls are concerned. Press or click taps the key once; hold keeps it down until you release it, so hold key \"w\" walks forward. Key names are the same as in on key press of, such as \"w\", \"space\", \"left shift\", \"shift\" or \"mouse left\".",
        "The key name has to be plain quoted text; an unknown key is an error when the script loads. It drives key bindings only and does not type into text boxes. Held keys stay held after the trigger ends until released, stop all scripts runs, or you leave the world. Key Is Held reads the real keyboard and does not see keys held this way."})
@Examples({"on key press of \"g\":",
        "\thold key \"w\"",
        "\twait 2 seconds",
        "\trelease key \"w\"",
        "",
        "on key press of \"j\":",
        "\thold key \"left shift\"",
        "\tpress key \"space\"",
        "\twait 10 ticks",
        "\trelease key \"left shift\""})
@Since("1.0.0-alpha")
public final class EffKey implements Statement {
    private final int line;
    private final String keyId;
    private final String action;

    private EffKey(int line, String keyId, String action) {
        this.line = line;
        this.keyId = keyId;
        this.action = action;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffKey::create, "(press:press|press:click|hold:hold|release:release) [the] key %string%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        String action = match.has("press") ? "press" : match.has("hold") ? "hold" : "release";
        return Optional.of(new EffKey(scope.line(), KeyNames.keyIdOf(match.slot(0)), action));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        switch (action) {
            case "press" -> context.world().clickKey(keyId);
            case "hold" -> context.world().setKeyHeld(keyId, true);
            default -> context.world().setKeyHeld(keyId, false);
        }
        return Flow.CONTINUE;
    }
}
