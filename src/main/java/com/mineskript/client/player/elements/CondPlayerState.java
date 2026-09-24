package com.mineskript.client.player.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Is Sneaking / Sprinting / On Ground")
@Description("Checks whether you are sneaking, sprinting, or standing on the ground. On ground is false while jumping, falling, flying or swimming.")
@Examples({
        "on key press of \"r\":",
        "	if player is sneaking:",
        "		send \"sneak + r\"",
        "",
        "every 5 ticks:",
        "	if player is not on the ground:",
        "		if fall distance is greater than 3:",
        "			show action bar \"falling %fall distance% blocks\"",
        "",
        "every 1 second:",
        "	if player is not sprinting:",
        "		if hunger of player is greater than 6:",
        "			show action bar \"you could sprint\""
})
@Since("1.0.0-alpha")
public final class CondPlayerState implements Condition {
    private static final String STATES = "(sneaking:sneaking|ground:on [the] ground|sprinting:sprinting)";

    private final String state;
    private final boolean negate;

    private CondPlayerState(String state, boolean negate) {
        this.state = state;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, scope, match.patternIndex() == 1),
                "%player% (is|are) " + STATES,
                "%player% (isn't|is not|aren't|are not) " + STATES);
    }

    private static Optional<Condition> create(Match match, ParseScope scope, boolean negate) {
        String state = match.has("sneaking") ? "sneaking" : match.has("ground") ? "ground" : "sprinting";
        return Optional.of(new CondPlayerState(state, negate));
    }

    @Override
    public boolean test(Context context) {
        boolean value = switch (state) {
            case "sneaking" -> context.world().isSneaking();
            case "ground" -> context.world().isOnGround();
            default -> context.world().isSprinting();
        };
        return negate != value;
    }
}
