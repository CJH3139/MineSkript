package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Add")
@Description({"Adds a number to a number variable, or adds values to a list variable. An unset variable counts as 0, so add 1 to {kills} works the first time. Give is accepted as another word for add.",
        "Adding to a list variable such as {names::*} stores each value under the lowest free number index, 1, 2, 3 and so on. The yaw, the pitch and the selected slot can be added to as well; the selected slot wraps round within 0 to 8.",
        "It does not add items to your inventory. Adding text to a single variable, or adding to a variable that holds text, is a run time error."})
@Examples({"on entity death:",
        "\tif event-entity is \"zombie\":",
        "\t\tadd 1 to {zombies killed}",
        "\t\tshow action bar \"zombies: %{zombies killed}%\"",
        "",
        "on chat:",
        "\tadd message to {-recent chat::*}",
        "",
        "on key press of \"q\":",
        "\tadd 90 to yaw"})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class EffAdd {
    private EffAdd() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.COMBINED,
                (match, scope) -> VariableChange.create(scope, ChangeMode.ADD, match.slot(1), match.slot(0)), "(add|give) %objects% to %objects%");
    }
}
