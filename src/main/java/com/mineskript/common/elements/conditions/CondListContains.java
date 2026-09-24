package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("List Contains")
@Description("The same check as Is In written the other way round: true when a list has an entry equal to the value. Text entries ignore capitals, as in Is. Used with a list on the left, such as online player names or a list variable like {friends::*}; when the left side is a single text value the Text Contains condition is used instead.")
@Examples({
        "on player join:",
        "	if online player names contains \"Alex\":",
        "		send \"Alex is here\"",
        "",
        "on player leave:",
        "	if online player names doesn't contain \"Alex\":",
        "		send \"Alex left\"",
        "",
        "on key press of \"f\":",
        "	if {friends::*} contains \"Alex\":",
        "		send \"Alex is a friend\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8", "1.0.0-alpha.11"})
public final class CondListContains {
    private CondListContains() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Membership.create(match, 1, 0, match.patternIndex() == 1),
                "%objects% (contains|contain) %object%",
                "%objects% (doesn't contain|does not contain|don't contain|do not contain) %object%");
    }
}
