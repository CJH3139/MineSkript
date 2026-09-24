package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Delete")
@Description({"Removes a variable, so it reads as unset again and Is Set is false for it. Clear is accepted as another word. Deleting a saved {global} variable removes it from the save file too.",
        "Deleting a whole list variable such as {names::*} removes every entry, including nested ones such as {names::a::b}; deleting {names::alex} removes one entry. Values of the game such as the yaw cannot be deleted."})
@Examples({"on key press of \"h\":",
        "	if key \"shift\" is held:",
        "		delete {home.x}",
        "		delete {home.y}",
        "		delete {home.z}",
        "		send \"home cleared\"",
        "",
        "on server disconnect:",
        "	clear {-seen::*}"})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class EffDelete {
    private EffDelete() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.COMBINED,
                (match, scope) -> VariableChange.create(scope, ChangeMode.DELETE, match.slot(0), null), "(delete|clear) %objects%");
    }
}
