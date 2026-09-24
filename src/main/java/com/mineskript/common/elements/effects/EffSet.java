package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.parse.Priority;
import com.mineskript.lang.parse.SyntaxRegistry;

@Name("Set")
@Description({"Stores a value in a variable, or changes a value of the game. The value can be anything: text, a number, a boolean, a timespan, an item type, an item, an entity, a game mode, or a list such as the result of split. {name} variables are saved to disk, {-name} variables last until the game closes, and {_name} variables belong to the one trigger run.",
        "Setting a whole list variable such as {names::*} replaces the list: the old entries are deleted and the new values are stored under the indices 1, 2, 3 and so on. Setting {names::alex} sets one entry.",
        "Besides variables, the yaw, the pitch, the selected slot, the clipboard and the message of on chat send and on command send can be set. Setting anything else is a parse error: can only set variables, or a message naming what that value does allow."})
@Examples({"on key press of \"h\":",
        "	set {home.x} to player's x-coordinate",
        "	set {home.y} to player's y-coordinate",
        "	set {home.z} to player's z-coordinate",
        "	send \"home set\"",
        "",
        "on held item change:",
        "	set {-last item} to event-previous item",
        "",
        "on key press of \"l\":",
        "	set {_colours::*} to \"red\", \"green\" and \"blue\"",
        "	set {homes::%player%} to player's y-coordinate",
        "",
        "on chat send:",
        "	set message to \"[me] %message%\""})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class EffSet {
    private EffSet() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(Priority.COMBINED,
                (match, scope) -> VariableChange.create(scope, ChangeMode.SET, match.slot(0), match.slot(1)), "set %objects% to %objects%");
    }
}
