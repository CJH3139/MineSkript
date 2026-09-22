package com.mineskript.syntax.effects;

import com.mineskript.game.KeyNames;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

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
