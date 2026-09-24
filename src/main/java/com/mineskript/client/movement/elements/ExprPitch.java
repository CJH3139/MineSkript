package com.mineskript.client.movement.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Name("Pitch")
@Description({
        "The angle you are looking up or down, in degrees, as a decimal number: -90 is straight up, 0 is level and 90 is straight down. Written pitch, pitch of player or player's pitch, like Skript. Needs a world: outside a world the line stops with a \"no world\" error.",
        "It can be set, added to and removed from. Adding or removing stops at straight up or straight down instead of going past it."
})
@Examples({
        "every 1 second:",
        "\tif pitch is greater than 80:",
        "\t\tshow action bar \"looking at your feet\"",
        "",
        "on key press of \"u\":",
        "\tremove 15 from pitch",
        "",
        "on key press of \"k\":",
        "\tset pitch of player to 0"
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8", "1.0.0-alpha.11"})
public final class ExprPitch extends GameValueExpression implements Changeable {
    private static final double STRAIGHT_UP = -90;
    private static final double STRAIGHT_DOWN = 90;

    private ExprPitch() {
        super(SkType.NUMBER, GameBridge::pitch);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprPitch()),
                "[the] pitch [of %players%]",
                "%players%'s pitch");
    }

    @Override
    public String changeName() {
        return "the pitch";
    }

    @Override
    public Set<ChangeMode> changeModes() {
        return EnumSet.of(ChangeMode.SET, ChangeMode.ADD, ChangeMode.REMOVE);
    }

    @Override
    public SkType changeType(ChangeMode mode) {
        return SkType.NUMBER;
    }

    @Override
    public void change(Context context, ChangeMode mode, Object value) {
        GameBridge game = context.world();
        double pitch = changed(mode, game.pitch(), value);
        game.setPitch(Math.max(STRAIGHT_UP, Math.min(STRAIGHT_DOWN, pitch)));
    }
}
