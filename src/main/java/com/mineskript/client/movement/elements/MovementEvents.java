package com.mineskript.client.movement.elements;

import com.mineskript.game.KeyNames;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

/** The key events: keys and mouse buttons going down and up. */
public final class MovementEvents {
    private MovementEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEvent("Key Press", (match, scope) -> {
            KeyNames.Combo combo = KeyNames.comboOf(match.slot(0));
            return Optional.of(new Event.KeyPress(combo.keyId(), combo.modifiers()));
        }, "on [key] press of %string%", "on key combo %string%")
                .description("Fires when a key or mouse button goes down. Keys are polled once per tick while you are in a world with no screen open, so typing in chat or menus never fires it. The key is a fixed text such as \"r\", \"space\", \"left shift\", \"f5\" or \"mouse left\", and a combo such as \"ctrl+shift+x\" fires when the last key goes down while all the modifiers are held (ctrl, shift, alt and win match either side).",
                        "The first poll after loading only records the current state, so a key already held does not fire. The physical keyboard and mouse are read, so keys pressed by a script with press key do not fire it, and a very quick tap between two ticks can be missed.")
                .examples("on key press of \"r\":",
                        "\tsend \"reloading\"",
                        "",
                        "on key combo \"ctrl+g\":",
                        "\tset {home x} to player's x-coordinate",
                        "\tsend \"home saved\"")
                .since("1.0.0-alpha");
        registry.addEvent("Key Release", (match, scope) -> Optional.of(new Event.KeyRelease(KeyNames.keyIdOf(match.slot(0)))), "on [key] release of %string%")
                .description("Fires when a watched key or mouse button goes back up, checked once per tick while you are in a world. It uses the same key names as key press, but takes a single key only; combos with + are not accepted here.",
                        "Opening any screen (chat, inventory, pause menu) while the key is held counts as a release, because keys read as up whenever a screen is open.")
                .examples("on key release of \"v\":",
                        "\trelease the use key",
                        "",
                        "on release of \"mouse right\":",
                        "\tsend \"let go of right click\"")
                .since("1.0.0-alpha");
    }
}
