package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Is Between")
@Description("Checks that a value lies between two bounds, inclusive at both ends, so 5 is between 1 and 5. The low bound must come first: nothing is between 9 and 1. If any of the three values is unset the result is false. None of the three may be a list. The negated form is not between is true when the value is outside the inclusive range, and also when any of the three values is unset.")
@Examples({
        "every 2 seconds:",
        "	if health of player is between 1 and 6:",
        "		show action bar \"careful, %health of player% hp\"",
        "",
        "on level change:",
        "	if xp level is not between 0 and 29:",
        "		send \"30 levels or more\""
})
@Since("1.0.0-alpha")
public final class CondIsBetween {
    private CondIsBetween() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Comparison.createBetween(match, match.patternIndex() == 1),
                "%objects% (is|are) between %objects% and %objects%",
                "%objects% (is not|isn't|aren't|are not) between %objects% and %objects%");
    }
}
