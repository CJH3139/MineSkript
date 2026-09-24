package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Relation;

@Name("Is At Least")
@Description("Checks that a number or timespan is greater than or equal to another. Also written greater than or equal to, more than or equal to, at least, or >=. An unset value on either side makes it false.")
@Examples({
        "every 1 second:",
        "	if health of player is at least 20:",
        "		show action bar \"full health\"",
        "",
        "on level change:",
        "	if xp level >= 30:",
        "		send \"enough levels to enchant\""
})
@Since("1.0.0-alpha.2")
public final class CondIsAtLeast {
    private CondIsAtLeast() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Comparison.create(match, Relation.GREATER_OR_EQUAL, false),
                "%objects% (is|are) ((greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|at least) %objects%",
                "%objects% >= %objects%");
    }
}
