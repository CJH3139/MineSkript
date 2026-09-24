package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.PlayerRef;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Player")
@Description({
        "You, the player running the script. Also written me or myself. Used as the target of player conditions and properties (player is sneaking, health of player, block below player). Printed in text it shows your username, which needs a world.",
        "MineSkript is client-side, so player always means you. Other players appear as entities (see nearest player)."
})
@Examples({
        "on key press of \"n\":",
        "	send \"hello %player%\"",
        "",
        "on key press of \"n\":",
        "	if me is sneaking:",
        "		send \"%myself% is sneaking\""
})
@Since("1.0.0-alpha")
public final class ExprPlayer implements Expression {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.PLAYER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprPlayer()),
                "[the] player",
                "me",
                "myself");
    }

    @Override
    public SkType type() {
        return SkType.PLAYER;
    }

    @Override
    public Object evaluate(Context context) {
        return PlayerRef.LOCAL;
    }
}
