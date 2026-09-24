package com.mineskript.client.movement.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Attack And Use")
@Description({"Presses, holds or releases your attack (left click) or use (right click) control. Press and click make a single click, exactly like tapping the button once. Hold keeps the control down until a matching release, so hold attack mines a block or hold use keeps eating or drawing a bow. It works through your key bindings, so it does whatever the button would do in game.",
        "A held control stays held after the trigger ends, until a release, stop all scripts, or leaving the world releases it. Click does nothing if the control has no key bound. Needs a world."})
@Examples({"on key press of \"r\":",
        "\thold attack",
        "\twait 20 ticks",
        "\trelease attack",
        "",
        "every 1 second:",
        "\tif key \"left alt\" is held:",
        "\t\tclick the attack button"})
@Since("1.0.0-alpha")
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
