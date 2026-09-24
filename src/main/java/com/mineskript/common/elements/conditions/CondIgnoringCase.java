package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Is Ignoring Case")
@Description("Compares two pieces of text for equality without caring about upper and lower case, so \"Hello\" is \"hello\" ignoring case is true. Plain Is compares text exactly.")
@Examples({
        "on chat send:",
        "	if message is \"gg\" ignoring case:",
        "		show action bar \"good game\"",
        "",
        "on key press of \"b\":",
        "	if server brand is not \"vanilla\" ignoring case:",
        "		send \"server runs %server brand%\""
})
@Since("1.0.0-alpha.2")
public final class CondIgnoringCase {
    private CondIgnoringCase() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> TextCheck.create(match, TextCheck.Kind.SAME, match.patternIndex() == 1),
                "%string% (is|are) %string% ignoring case",
                "%string% (isn't|is not|aren't|are not) %string% ignoring case");
    }
}
