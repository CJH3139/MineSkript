package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Relation;

@Name("Is Greater Than")
@Description("Checks that a number or timespan is strictly greater than another. Also written more than, higher than, bigger than, larger than, above, or >. An unset value on either side makes it false. Against a list joined with or, one entry is enough; joined with and, it must be greater than every entry.")
@Examples({
        "on damage:",
        "	if event-damage is greater than 5:",
        "		send \"big hit: %event-damage%\""
})
@Since("1.0.0-alpha")
public final class CondIsGreaterThan {
    private CondIsGreaterThan() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Comparison.create(match, Relation.GREATER, false),
                "%objects% (is|are) ((greater|more|higher|bigger|larger) than|above|>) %objects%",
                "%objects% > %objects%");
    }
}
