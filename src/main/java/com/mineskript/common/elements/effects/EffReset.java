package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Reset")
@Description("Puts a value back to its default. For a variable or a list variable that means deleting it, the same as Delete. Values of the game such as the yaw have no default and cannot be reset.")
@Examples({"on server join:",
        "	reset {-kills this session}",
        "	reset {-seen::*}"})
@Since("1.0.0-alpha.8")
public final class EffReset {
    private EffReset() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.COMBINED,
                (match, scope) -> VariableChange.create(scope, ChangeMode.RESET, match.slot(0), null), "reset %objects%");
    }
}
