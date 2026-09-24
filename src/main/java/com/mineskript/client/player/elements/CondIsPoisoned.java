package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Is Poisoned")
@Description("Checks whether you have the poison effect, like Skript's is poisoned. It always reads your own player.")
@Examples({
        "every 1 second:",
        "	if player is poisoned:",
        "		show action bar \"poisoned, drink milk\"",
        "",
        "on effect lose:",
        "	if player is not poisoned:",
        "		send \"the poison wore off\""
})
@Since("1.0.0-alpha.11")
public final class CondIsPoisoned implements Condition {
    private static final String POISON = "minecraft:poison";

    private final boolean negate;

    private CondIsPoisoned(boolean negate) {
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> Optional.of(new CondIsPoisoned(match.patternIndex() == 1)),
                "%players% (is|are) poisoned",
                "%players% (isn't|is not|aren't|are not) poisoned");
    }

    @Override
    public boolean test(Context context) {
        return negate != context.world().activeEffects().containsKey(POISON);
    }
}
