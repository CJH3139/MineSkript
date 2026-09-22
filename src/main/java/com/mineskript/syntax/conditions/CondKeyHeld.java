package com.mineskript.syntax.conditions;

import com.mineskript.game.KeyNames;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

public final class CondKeyHeld implements Condition {
    private final String keyId;
    private final boolean negate;

    private CondKeyHeld(String keyId, boolean negate) {
        this.keyId = keyId;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> Optional.of(new CondKeyHeld(KeyNames.keyIdOf(match.slot(0)), match.has("not"))),
                "key %string% (is|not:isn't|not:is not) (held|pressed|down)");
    }

    @Override
    public boolean test(Context context) {
        return negate != context.game().isKeyDown(keyId);
    }
}
