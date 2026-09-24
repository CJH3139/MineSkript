package com.mineskript.client.hud.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Close Screen")
@Description({"Closes whatever screen is open: an inventory, a chest, a crafting table, the chat box or the pause menu, just as pressing Escape would. Does nothing when no screen is open.",
        "Needs a world."})
@Examples({"on screen open:",
        "	if event-screen title contains \"Vote\":",
        "		wait 1 tick",
        "		close screen"})
@Since("1.0.0-alpha.2")
public final class EffCloseScreen implements Statement {
    private final int line;

    private EffCloseScreen(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffCloseScreen(scope.line())),
                "close [the] screen",
                "close [the] (inventory|gui)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().closeScreen();
        return Flow.CONTINUE;
    }
}
