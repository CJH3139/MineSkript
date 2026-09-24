package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Ends With")
@Description("Checks whether text ends with another piece of text. Case-sensitive.")
@Examples({
        "on chat:",
        "	if message ends with \"?\":",
        "		show action bar \"someone asked a question\"",
        "",
        "on chat send:",
        "	if message doesn't end with \".\":",
        "		send \"tip: end sentences with a full stop\""
})
@Since("1.0.0-alpha.2")
public final class CondEndsWith {
    private CondEndsWith() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> TextCheck.create(match, TextCheck.Kind.ENDS, match.patternIndex() == 1),
                "%string% (ends with|end with) %string%",
                "%string% (doesn't end with|does not end with|don't end with|do not end with) %string%");
    }
}
