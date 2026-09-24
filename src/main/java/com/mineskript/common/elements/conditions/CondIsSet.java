package com.mineskript.common.elements.conditions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.List;
import java.util.Optional;

@Name("Is Set")
@Description("Checks whether a value exists. A variable that was never set or has been deleted is not set, and neither is an expression with no value, such as the nearest entity when none is loaded. Several values joined with and or commas are set only when every one of them is; a list with no entries is never set. A list variable such as {homes::*} is set when it has at least one entry.")
@Examples({
        "on key press of \"g\":",
        "	if {home} is set:",
        "		send \"you have a home\"",
        "	else:",
        "		set {home} to \"%player's x-coordinate% %player's y-coordinate% %player's z-coordinate%\"",
        "		send \"home saved\"",
        "",
        "every 1 second:",
        "	if target entity is set:",
        "		show action bar \"looking at %name of target entity%\"",
        "",
        "on load:",
        "	if {deaths} is not set:",
        "		set {deaths} to 0"
})
@Since("1.0.0-alpha.4")
public final class CondIsSet implements Condition {
    private final Expression value;
    private final boolean negate;

    private CondIsSet(Expression value, boolean negate) {
        this.value = value;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition((match, scope) -> Optional.of(new CondIsSet(match.slot(0), match.patternIndex() == 1)),
                "%objects% (is|are) set",
                "%objects% (isn't|is not|aren't|are not) set");
    }

    @Override
    public boolean test(Context context) {
        return negate != isSet(value.evaluate(context));
    }

    private static boolean isSet(Object value) {
        if (value instanceof List<?> list) {
            return !list.isEmpty() && list.stream().allMatch(CondIsSet::isSet);
        }
        return value != null && value != None.NONE;
    }
}
