package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Relation;

@Name("Is At Most")
@Description("Checks that a number or timespan is less than or equal to another. Also written less than or equal to, at most, or <=. An unset value on either side makes it false.")
@Examples({
        "on hunger change:",
        "	if hunger of player is at most 6:",
        "		show title \"eat something\""
})
@Since("1.0.0-alpha.2")
public final class CondIsAtMost {
    private CondIsAtMost() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Comparison.create(match, Relation.LESS_OR_EQUAL, false),
                "%objects% (is|are) ((less|smaller|lower|below) [than] or (equal to|the same as)|at most) %objects%",
                "%objects% <= %objects%");
    }
}
