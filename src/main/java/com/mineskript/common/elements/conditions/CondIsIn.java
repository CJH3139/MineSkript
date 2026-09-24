package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Is In (List)")
@Description({
        "Checks whether a value equals any entry of a list, such as online player names, a split text, or a list written in the script with commas, and, or. Entries are compared the same way as Is, so text is case-sensitive. An empty list never contains anything. The negated form is not in is also true when the list is empty.",
        "The right side has to be a list: a list expression or a list variable such as {friends::*}. A single variable such as {x} is not treated as a list here, even if it was set from split, so store the pieces in a list variable instead."
})
@Examples({
        "on key press of \"n\":",
        "	if \"Notch\" is in online player names:",
        "		send \"Notch is online\"",
        "",
        "every 1 second:",
        "	if block below player is in sand, gravel or red sand:",
        "		show action bar \"falling block below you\"",
        "",
        "on player join:",
        "	if \"Steve\" is not in online player names:",
        "		send \"Steve is still away\"",
        "",
        "on chat:",
        "	if \"Notch\" is in {-friends::*}:",
        "		send \"a message while a friend is around\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class CondIsIn {
    private CondIsIn() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Membership.create(match, 0, 1, match.patternIndex() == 1),
                "%object% (is|are) in %objects%",
                "%object% (is not|isn't|aren't|are not) in %objects%");
    }
}
