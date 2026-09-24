package com.mineskript.client.world.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Weather")
@Description("Checks the weather of the world you are in: it is raining, or it is thundering. This is the world-wide weather the game reports, not the biome, so it can say raining while you stand in a desert where no rain falls.")
@Examples({
        "on weather change:",
        "	if it is thundering:",
        "		show title \"thunderstorm\"",
        "",
        "every 30 seconds:",
        "	if it is raining:",
        "		if player can see the sky:",
        "			show action bar \"you are in the rain\"",
        "",
        "on weather change:",
        "	if it is not raining:",
        "		send \"the rain stopped\""
})
@Since("1.0.0-alpha.2")
public final class CondWeather implements Condition {
    private static final String KINDS = "(raining:raining|thundering:thundering)";

    private final boolean raining;
    private final boolean negate;

    private CondWeather(boolean raining, boolean negate) {
        this.raining = raining;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> create(match, match.patternIndex() == 1),
                "it is " + KINDS,
                "it (isn't|is not) " + KINDS);
    }

    private static Optional<Condition> create(Match match, boolean negate) {
        return Optional.of(new CondWeather(match.has("raining"), negate));
    }

    @Override
    public boolean test(Context context) {
        boolean value = raining ? context.world().isRaining() : context.world().isThundering();
        return negate != value;
    }
}
