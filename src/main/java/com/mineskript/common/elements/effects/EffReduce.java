package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Reduce")
@Description("The same as Remove written the other way round: reduce {x} by 2 subtracts 2 from {x}. Decrease is also accepted. An unset variable counts as 0. Works on anything Remove works on, such as decrease pitch by 10.")
@Examples({"every 1 second:",
        "	if {-cooldown} is greater than 0:",
        "		decrease {-cooldown} by 1",
        "",
        "on key press of \"r\":",
        "	reduce selected slot by 1"})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class EffReduce {
    private EffReduce() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.COMBINED,
                (match, scope) -> VariableChange.create(scope, ChangeMode.REMOVE, match.slot(0), match.slot(1)), "(reduce|decrease) %objects% by %objects%");
    }
}
