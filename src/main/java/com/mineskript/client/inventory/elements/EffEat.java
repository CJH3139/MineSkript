package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.Language;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Eat Item")
@Description({"Eats or drinks the item in your hand by holding the use button until the game says you are no longer using an item, then letting go. The trigger waits while this happens, so the next line runs once you have finished eating.",
        "If the item cannot be eaten, the button is held for about 2 ticks and then released. With an item that stays in use while the button is down, such as a bow or shield, the trigger keeps waiting until something else stops the use. Written eat, drink or consume, followed by optional held or holding and then item or food."})
@Examples({"on hunger change:",
        "	if hunger of player is less than 14:",
        "		if player is holding cooked beef:",
        "			eat held item",
        "			send \"ate, hunger now %hunger of player%\""})
@Since("1.0.0-alpha.4")
public final class EffEat implements Statement {
    private static final int START_TICKS = 2;

    private final int line;
    private final Block steps;

    private EffEat(int line) {
        this.line = line;
        this.steps = new Block(List.of(
                new Pause(line),
                new WaitUntil(line, context -> !context.world().isUsingItem()),
                new Release(line)));
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> {
            if (!scope.canWait()) {
                throw new SyntaxException(Language.get("parse.cannot-wait-here"));
            }
            return Optional.of(new EffEat(scope.line()));
        },
                "(eat|drink|consume) [the] [(held|holding)] (item|food)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().setUseHeld(true);
        return new Flow.Enter(steps);
    }

    private record Pause(int line) implements Statement {
        @Override
        public Flow execute(Context context) {
            return new Flow.Wait(START_TICKS);
        }
    }

    private record Release(int line) implements Statement {
        @Override
        public Flow execute(Context context) {
            context.world().setUseHeld(false);
            return Flow.CONTINUE;
        }
    }
}
