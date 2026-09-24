package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Increase")
@Description("The same as Add written the other way round: increase {x} by 5 adds 5 to {x}. An unset variable counts as 0. Works on anything Add works on, such as increase yaw by 45.")
@Examples({"on death:",
        "	increase {deaths} by 1",
        "	send \"deaths so far: %{deaths}%\"",
        "",
        "on key press of \"e\":",
        "	increase selected slot by 1"})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class EffIncrease {
    private EffIncrease() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.COMBINED,
                (match, scope) -> VariableChange.create(scope, ChangeMode.ADD, match.slot(0), match.slot(1)), "increase %objects% by %objects%");
    }
}
