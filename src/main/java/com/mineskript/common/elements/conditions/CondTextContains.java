package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Text Contains")
@Description("Checks whether one piece of text appears inside another. The check is case-sensitive; lowercase both sides first to ignore case. Both sides must be single values, not lists.")
@Examples({
        "on chat:",
        "	if message contains \"your turn\":",
        "		play sound \"minecraft:block.note_block.pling\"",
        "",
        "on chat send:",
        "	if lowercase message contains \"password\":",
        "		cancel event",
        "		send \"blocked a message containing a password\"",
        "",
        "on chat send:",
        "	if message doesn't contain \" \":",
        "		send \"one word message\""
})
@Since("1.0.0-alpha.2")
public final class CondTextContains {
    private CondTextContains() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> TextCheck.create(match, TextCheck.Kind.CONTAINS, match.patternIndex() == 1),
                "%string% (contains|contain) %string%",
                "%string% (doesn't contain|does not contain|don't contain|do not contain) %string%");
    }
}
