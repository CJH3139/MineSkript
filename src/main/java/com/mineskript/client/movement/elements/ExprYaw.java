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

@Name("Yaw")
@Description({
        "The direction you are facing horizontally, in degrees, as a decimal number. 0 faces south (+z), 90 west, 180 north and -90 east. Needs a world: outside a world the line stops with a \"no world\" error.",
        "The game does not wrap this value, so after turning around several times it can be far outside -180 to 180. It can be set, added to and removed from, so add 90 to yaw turns you a quarter to the right."
})
@Examples({
        "on key press of \"y\":",
        "\tsend \"yaw %yaw%, pitch %pitch%\"",
        "",
        "on key press of \"t\":",
        "\tadd 180 to yaw"
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class ExprYaw extends GameValueExpression implements Changeable {
    private ExprYaw() {
        super(SkType.NUMBER, GameBridge::yaw);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprYaw()), "[the] yaw");
    }

    @Override
    public String changeName() {
        return "the yaw";
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
        game.setYaw(changed(mode, game.yaw(), value));
    }
}
