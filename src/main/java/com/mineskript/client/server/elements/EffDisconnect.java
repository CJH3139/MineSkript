package com.mineskript.client.server.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Disconnect")
@Description("Leaves the server or singleplayer world you are in and returns to the menus. The trigger stops here, so nothing after it runs. Also written leave the server, quit the game and similar.")
@Examples({"every 1 second:",
        "	if health of player is less than 4:",
        "		if {-safety} is true:",
        "			disconnect"})
@Since("1.0.0-alpha.2")
public final class EffDisconnect implements Statement {
    private final int line;

    private EffDisconnect(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffDisconnect(scope.line())),
                "disconnect",
                "(leave|quit) [the] (server|world|game)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().disconnect();
        return Flow.STOP;
    }
}
