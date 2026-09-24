package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Remove")
@Description({"Subtracts a number from a number variable, or removes values from a list variable. An unset variable counts as 0, so it becomes negative. Subtract is accepted as another word for remove.",
        "From a list variable such as {names::*}, remove x deletes the first entry equal to x, as in Skript, and remove all x deletes every entry equal to x. The other entries keep their indices. The yaw, the pitch and the selected slot can be removed from as well.",
        "It cannot remove items from your inventory."})
@Examples({"on block place:",
        "\tif {-blocks left} is set:",
        "\t\tremove 1 from {-blocks left}",
        "\t\tshow action bar \"%{-blocks left}% blocks left\"",
        "",
        "on player leave:",
        "\tremove all event-player from {-seen::*}"})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class EffRemove {
    private EffRemove() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.COMBINED, (match, scope) -> VariableChange.create(scope,
                        match.patternIndex() == 0 ? ChangeMode.REMOVE_ALL : ChangeMode.REMOVE, match.slot(1), match.slot(0)),
                "(remove|subtract) all [of] %objects% from %objects%",
                "(remove|subtract) %objects% from %objects%");
    }
}
