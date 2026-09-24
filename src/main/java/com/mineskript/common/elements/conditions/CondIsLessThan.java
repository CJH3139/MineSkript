package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Relation;

@Name("Is Less Than")
@Description({
        "Checks that a number or timespan is strictly less than another. Also written smaller than, lower than, below, or <. An unset value on either side makes it false.",
        "Ordering is only accepted for numbers and timespans. Two variables are checked at run time, and a number against text fails with cannot compare."
})
@Examples({
        "every 5 seconds:",
        "	if health of player is less than 6:",
        "		show title \"low health\""
})
@Since("1.0.0-alpha")
public final class CondIsLessThan {
    private CondIsLessThan() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Comparison.create(match, Relation.LESS, false),
                "%objects% (is|are) ((less|smaller|lower) than|below|<) %objects%",
                "%objects% < %objects%");
    }
}
