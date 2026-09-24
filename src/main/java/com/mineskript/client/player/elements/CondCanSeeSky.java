package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Can See Sky")
@Description({
        "Checks whether the block you are standing in has a clear view of the sky, with nothing solid above it. It always reads your own player, whichever player word you write. Useful for telling whether you are underground or under a roof.",
        "Needs a world: used from the title screen it stops the trigger with a no world error."
})
@Examples({
        "every 5 seconds:",
        "	if player can see the sky:",
        "		if it is thundering:",
        "			show action bar \"storm overhead, find cover\"",
        "",
        "on key press of \"u\":",
        "	if player cannot see sky:",
        "		send \"you are under cover at y %player's y-coordinate%\"",
        "",
        "every 10 seconds:",
        "	if player can't see the sky:",
        "		show action bar \"underground\""
})
@Since("1.0.0-alpha.2")
public final class CondCanSeeSky implements Condition {
    private final boolean negate;

    private CondCanSeeSky(boolean negate) {
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> Optional.of(new CondCanSeeSky(match.patternIndex() == 1)),
                "%player% (can see|sees) [the] sky",
                "%player% (can't see|cannot see|can not see|doesn't see|does not see) [the] sky");
    }

    @Override
    public boolean test(Context context) {
        return negate != context.world().canSeeSky();
    }
}
