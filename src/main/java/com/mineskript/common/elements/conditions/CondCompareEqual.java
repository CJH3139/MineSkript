package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Relation;

@Name("Is (Equal)")
@Description({
        "Compares two values for equality. Text is compared exactly, so case matters; blocks, items and block types match when they are the same kind of block or item; an entity matches text naming its type, such as \"zombie\" or \"minecraft:zombie\". When the right side is a list joined with or, one match is enough; joined with and, every entry must match. An unset value is never equal to anything, and an empty list never matches. The negated form is not is true when the values differ: against an or list it means none of them match (\"is not stone or dirt\" means neither), and since an unset value is never equal, {x} is not 5 is true while {x} is unset.",
        "The left side must be a single value, not a list. Values that can never be compared, such as text against a number, are refused when the script loads; a variable holding the wrong kind of value fails at run time with cannot compare. Use Is Ignoring Case to ignore capitals."
})
@Examples({
        "on key press of \"r\":",
        "	if block below player is stone or cobblestone:",
        "		send \"standing on stone\"",
        "",
        "on chat send:",
        "	if message is \"afk\":",
        "		set {-afk} to true",
        "		show title \"AFK\"",
        "",
        "on key press of \"r\":",
        "	if block below player is not stone or dirt:",
        "		send \"not on stone or dirt\""
})
@Since("1.0.0-alpha")
public final class CondCompareEqual {
    private CondCompareEqual() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(Priority.PATTERN_MATCHES_EVERYTHING,
                (match, scope) -> Comparison.create(match, Relation.EQUAL, match.patternIndex() == 1),
                "%objects% (is|are|=) [(equal to|the same as)] %objects%",
                "%objects% (is not|isn't|aren't|are not|!=) [equal to] %objects%");
    }
}
