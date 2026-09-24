package com.mineskript.client.server.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Server Address")
@Description("The address of the server you are connected to, exactly as it appears in your server list (for example play.example.net). Returns singleplayer when no multiplayer server is set, which includes singleplayer worlds and the title screen. Also written server ip. Works without a world.")
@Examples({
        "on world join:",
        "\tsend \"joined %server address%\"",
        "",
        "on world join:",
        "\tif server address is \"singleplayer\":",
        "\t\tsend \"local world\""
})
@Since("1.0.0-alpha.2")
public final class ExprServerAddress implements Expression {
    private ExprServerAddress() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprServerAddress()), "[the] server (address|ip)");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.game().serverAddress();
    }
}
