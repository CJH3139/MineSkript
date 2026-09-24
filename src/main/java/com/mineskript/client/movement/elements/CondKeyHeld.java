package com.mineskript.client.movement.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.KeyNames;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Key Is Held")
@Description({
        "Checks whether a physical key or mouse button is currently down. Key names are the same as in on key press of, such as \"w\", \"space\", \"left shift\", \"shift\", \"f5\" or \"mouse left\". The negation is part of the same syntax: key \"w\" is not held. Held, pressed and down all mean the same.",
        "The key name must be written as plain quoted text, and an unknown key is an error when the script loads. While any screen is open (chat, inventory, pause menu) every key reads as not held. It reads the real keyboard, so a key held by the Press Key effect does not count."
})
@Examples({
        "every 1 second:",
        "	if key \"left alt\" is held:",
        "		show action bar \"fps: %fps%\"",
        "",
        "on key press of \"r\":",
        "	if key \"shift\" is not held:",
        "		send \"hold shift and press r to confirm\"",
        "		stop",
        "	send \"confirmed\""
})
@Since("1.0.0-alpha")
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
