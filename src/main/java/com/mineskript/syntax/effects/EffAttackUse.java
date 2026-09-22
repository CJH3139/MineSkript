package com.mineskript.syntax.effects;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class EffAttackUse implements Statement {
    private enum Action {
        CLICK,
        HOLD,
        RELEASE
    }

    private final int line;
    private final Action action;
    private final boolean attack;

    private EffAttackUse(int line, Action action, boolean attack) {
        this.line = line;
        this.action = action;
        this.attack = attack;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffAttackUse::create,
                "(press|click) [the] (attack:attack|use:use) [(key|button)]",
                "hold [the] (attack:attack|use:use) [(key|button)]",
                "release [the] (attack:attack|use:use) [(key|button)]");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        Action action = Action.values()[match.patternIndex()];
        return Optional.of(new EffAttackUse(scope.line(), action, match.has("attack")));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        switch (action) {
            case CLICK -> {
                if (attack) {
                    game.clickAttack();
                } else {
                    game.clickUse();
                }
            }
            case HOLD -> {
                if (attack) {
                    game.setAttackHeld(true);
                } else {
                    game.setUseHeld(true);
                }
            }
            case RELEASE -> {
                if (attack) {
                    game.setAttackHeld(false);
                } else {
                    game.setUseHeld(false);
                }
            }
        }
        return Flow.CONTINUE;
    }
}
