package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Starts With")
@Description("Checks whether text begins with another piece of text. Capitals are ignored, like everywhere text is compared, so \"Hello\" starts with \"he\". Every text starts with the empty text \"\".")
@Examples({
        "on chat send:",
        "	if message starts with \"!\":",
        "		cancel event",
        "		send \"not sending a bot command to public chat\"",
        "",
        "on chat:",
        "	if message doesn't start with \"<\":",
        "		send \"server message: %message%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class CondStartsWith {
    private CondStartsWith() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> TextCheck.create(match, TextCheck.Kind.STARTS, match.patternIndex() == 1),
                "%string% (starts with|start with) %string%",
                "%string% (doesn't start with|does not start with|don't start with|do not start with) %string%");
    }
}
